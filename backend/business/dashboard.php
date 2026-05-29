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
$month = input_string($input, 'month', date('Y-m'));

try {
    $conn = (new Database())->connect();

    $stmt = $conn->prepare('SELECT COALESCE(SUM(balance), 0) AS totalBalance, COUNT(*) AS walletCount FROM wallets WHERE userId = ? AND isBusiness = 1');
    $stmt->bind_param('i', $userId);
    $stmt->execute();
    $wallets = normalize_row(fetch_one($stmt) ?: ['totalBalance' => 0, 'walletCount' => 0]);

    $stmt = $conn->prepare(
        'SELECT '
        . 'COALESCE(SUM(CASE WHEN isIncome = 1 THEN amount ELSE 0 END), 0) AS totalIncome, '
        . 'COALESCE(SUM(CASE WHEN isIncome = 0 THEN amount ELSE 0 END), 0) AS totalExpense, '
        . 'COUNT(*) AS transactionCount '
        . 'FROM transactions WHERE userId = ? AND isBusiness = 1 AND DATE_FORMAT(timestamp, "%Y-%m") = ?'
    );
    $stmt->bind_param('is', $userId, $month);
    $stmt->execute();
    $totals = normalize_row(fetch_one($stmt) ?: ['totalIncome' => 0, 'totalExpense' => 0, 'transactionCount' => 0]);

    $stmt = $conn->prepare(
        'SELECT t.id, t.amount, t.timestamp, t.note, t.categoryId, c.name AS categoryName, t.walletId, w.name AS walletName, t.isIncome '
        . 'FROM transactions t '
        . 'LEFT JOIN categories c ON c.id = t.categoryId '
        . 'LEFT JOIN wallets w ON w.id = t.walletId '
        . 'WHERE t.userId = ? AND t.isBusiness = 1 '
        . 'ORDER BY t.timestamp DESC LIMIT 5'
    );
    $stmt->bind_param('i', $userId);
    $stmt->execute();
    $recentTransactions = array_map('normalize_row', fetch_all($stmt));

    json_response(true, 'Dashboard loaded', [
        'userId' => $userId,
        'month' => $month,
        'totalBalance' => $wallets['totalBalance'],
        'walletCount' => (int) $wallets['walletCount'],
        'totalIncome' => $totals['totalIncome'],
        'totalExpense' => $totals['totalExpense'],
        'netProfit' => $totals['totalIncome'] - $totals['totalExpense'],
        'transactionCount' => (int) $totals['transactionCount'],
        'recentTransactions' => $recentTransactions,
    ]);
} catch (Throwable $e) {
    json_response(false, 'Server error: ' . $e->getMessage(), null, 500);
}
