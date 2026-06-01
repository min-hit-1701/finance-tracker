<?php
require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/request.php';
require_once __DIR__ . '/../helpers/db.php';
require_once __DIR__ . '/../helpers/auth.php';

allow_cors('GET, OPTIONS');
require_method(['GET']);

$input = request_data();
$isBusiness = (int) $apiMode;
$authUser = require_auth_user($input);
$userId = (int) $authUser['id'];

try {
    $conn = (new Database())->connect();

    $stmt = $conn->prepare('SELECT '
        . 'COALESCE(SUM(CASE WHEN isIncome = 1 THEN amount ELSE 0 END), 0) AS income, '
        . 'COALESCE(SUM(CASE WHEN isIncome = 0 THEN amount ELSE 0 END), 0) AS expense '
        . 'FROM transactions WHERE userId = ? AND isBusiness = ?');
    $stmt->bind_param('ii', $userId, $isBusiness);
    $stmt->execute();
    $totals = normalize_row(fetch_one($stmt) ?: ['income' => 0, 'expense' => 0]);

    json_response(true, 'Summary loaded', [
        'userId' => $userId,
        'isBusiness' => $isBusiness,
        'totalBalance' => $totals['income'] - $totals['expense'],
        'totalIncome' => $totals['income'],
        'totalExpense' => $totals['expense'],
    ]);
} catch (Throwable $e) {
    json_response(false, 'Server error: ' . $e->getMessage(), null, 500);
}
