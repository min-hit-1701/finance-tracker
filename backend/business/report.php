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

    $stmt = $conn->prepare(
        'SELECT '
        . 'COALESCE(SUM(CASE WHEN isIncome = 1 THEN amount ELSE 0 END), 0) AS totalIncome, '
        . 'COALESCE(SUM(CASE WHEN isIncome = 0 THEN amount ELSE 0 END), 0) AS totalExpense '
        . 'FROM transactions WHERE userId = ? AND isBusiness = 1 AND DATE_FORMAT(timestamp, "%Y-%m") = ?'
    );
    $stmt->bind_param('is', $userId, $month);
    $stmt->execute();
    $totals = normalize_row(fetch_one($stmt) ?: ['totalIncome' => 0, 'totalExpense' => 0]);

    $stmt = $conn->prepare(
        'SELECT c.id AS categoryId, c.name AS categoryName, COALESCE(SUM(t.amount), 0) AS totalExpense '
        . 'FROM transactions t '
        . 'INNER JOIN categories c ON c.id = t.categoryId '
        . 'WHERE t.userId = ? AND t.isBusiness = 1 AND t.isIncome = 0 AND DATE_FORMAT(t.timestamp, "%Y-%m") = ? '
        . 'GROUP BY c.id, c.name ORDER BY totalExpense DESC'
    );
    $stmt->bind_param('is', $userId, $month);
    $stmt->execute();
    $expensesByCategory = array_map('normalize_row', fetch_all($stmt));

    $stmt = $conn->prepare(
        'SELECT DATE_FORMAT(timestamp, "%Y-%m") AS month, '
        . 'COALESCE(SUM(CASE WHEN isIncome = 1 THEN amount ELSE 0 END), 0) AS totalIncome, '
        . 'COALESCE(SUM(CASE WHEN isIncome = 0 THEN amount ELSE 0 END), 0) AS totalExpense '
        . 'FROM transactions '
        . 'WHERE userId = ? AND isBusiness = 1 '
        . 'GROUP BY DATE_FORMAT(timestamp, "%Y-%m") '
        . 'ORDER BY month DESC LIMIT 6'
    );
    $stmt->bind_param('i', $userId);
    $stmt->execute();
    $monthlyTrend = array_reverse(array_map('normalize_row', fetch_all($stmt)));

    $stmt = $conn->prepare(
        'SELECT b.id, b.categoryId, c.name AS categoryName, b.amount AS budgetAmount, b.month, '
        . 'COALESCE(SUM(CASE WHEN t.isIncome = 0 THEN t.amount ELSE 0 END), 0) AS spentAmount '
        . 'FROM budgets b '
        . 'INNER JOIN categories c ON c.id = b.categoryId '
        . 'LEFT JOIN transactions t ON t.userId = b.userId AND t.categoryId = b.categoryId AND t.isBusiness = 1 AND DATE_FORMAT(t.timestamp, "%Y-%m") = b.month '
        . 'WHERE b.userId = ? AND c.isBusiness = 1 AND b.month = ? '
        . 'GROUP BY b.id, b.categoryId, c.name, b.amount, b.month '
        . 'ORDER BY spentAmount DESC'
    );
    $stmt->bind_param('is', $userId, $month);
    $stmt->execute();
    $budgetUsage = array_map(static function (array $row): array {
        $row = normalize_row($row);
        $row['percent'] = $row['budgetAmount'] > 0 ? round(($row['spentAmount'] / $row['budgetAmount']) * 100, 2) : 0.0;
        return $row;
    }, fetch_all($stmt));

    json_response(true, 'Report loaded', [
        'userId' => $userId,
        'month' => $month,
        'totalIncome' => $totals['totalIncome'],
        'totalExpense' => $totals['totalExpense'],
        'netProfit' => $totals['totalIncome'] - $totals['totalExpense'],
        'expensesByCategory' => $expensesByCategory,
        'monthlyTrend' => $monthlyTrend,
        'budgetUsage' => $budgetUsage,
    ]);
} catch (Throwable $e) {
    json_response(false, 'Server error: ' . $e->getMessage(), null, 500);
}
