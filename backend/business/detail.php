<?php
require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/request.php';
require_once __DIR__ . '/../helpers/db.php';
require_once __DIR__ . '/../helpers/auth.php';

allow_cors('GET, OPTIONS');
require_method(['GET']);

$input = request_data();
$authUser = require_auth_user($input);
$userId = (int) $authUser['id'];
$businessId = input_int($input, 'id');

if (!$businessId) {
    json_response(false, 'id is required', null, 422);
}

try {
    $conn = (new Database())->connect();

    $conn->query(
        'CREATE TABLE IF NOT EXISTS business_entities ('
        . 'id INT AUTO_INCREMENT PRIMARY KEY, '
        . 'userId INT NOT NULL, '
        . 'name VARCHAR(255) NOT NULL, '
        . 'type VARCHAR(120) NOT NULL, '
        . 'note TEXT NULL, '
        . 'createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, '
        . 'updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, '
        . 'INDEX idx_business_entities_user (userId)'
        . ') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci'
    );

    $stmt = $conn->prepare('SELECT id, userId, name, type, note, createdAt, updatedAt FROM business_entities WHERE id = ? AND userId = ? LIMIT 1');
    $stmt->bind_param('ii', $businessId, $userId);
    $stmt->execute();
    $business = fetch_one($stmt);

    if (!$business) {
        json_response(false, 'Business entity not found', null, 404);
    }

    $stmt = $conn->prepare('SELECT id, userId, name, balance, type, isBusiness FROM wallets WHERE userId = ? AND isBusiness = 1 ORDER BY id DESC');
    $stmt->bind_param('i', $userId);
    $stmt->execute();
    $wallets = array_map('normalize_row', fetch_all($stmt));

    $stmt = $conn->prepare(
        'SELECT id, userId, amount, timestamp, note, categoryId, walletId, isIncome, isBusiness '
        . 'FROM transactions WHERE userId = ? AND isBusiness = 1 ORDER BY timestamp DESC LIMIT 10'
    );
    $stmt->bind_param('i', $userId);
    $stmt->execute();
    $recentTransactions = array_map('normalize_row', fetch_all($stmt));

    $stmt = $conn->prepare(
        'SELECT '
        . 'COALESCE(SUM(CASE WHEN isIncome = 1 THEN amount ELSE 0 END), 0) AS totalIncome, '
        . 'COALESCE(SUM(CASE WHEN isIncome = 0 THEN amount ELSE 0 END), 0) AS totalExpense '
        . 'FROM transactions WHERE userId = ? AND isBusiness = 1'
    );
    $stmt->bind_param('i', $userId);
    $stmt->execute();
    $totals = normalize_row(fetch_one($stmt) ?: ['totalIncome' => 0, 'totalExpense' => 0]);

    json_response(true, 'Business detail loaded', [
        'business' => normalize_row($business),
        'summary' => [
            'totalIncome' => $totals['totalIncome'],
            'totalExpense' => $totals['totalExpense'],
            'netProfit' => $totals['totalIncome'] - $totals['totalExpense'],
        ],
        'wallets' => $wallets,
        'recentTransactions' => $recentTransactions,
    ]);
} catch (Throwable $e) {
    json_response(false, 'Server error: ' . $e->getMessage(), null, 500);
}
