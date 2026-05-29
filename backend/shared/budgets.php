<?php
require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/request.php';
require_once __DIR__ . '/../helpers/db.php';
require_once __DIR__ . '/../helpers/auth.php';

allow_cors();

$input = request_data();
$method = $_SERVER['REQUEST_METHOD'];
$isBusiness = (int) $apiMode;
$authUser = require_auth_user($input);
$userId = (int) $authUser['id'];

try {
    $conn = (new Database())->connect();

    if ($method === 'GET') {
        $month = input_string($input, 'month');
        $monthFilter = $month ?: '';

        $stmt = $conn->prepare(
            'SELECT b.id, b.userId, b.categoryId, c.name AS categoryName, b.amount, b.month, '
            . 'COALESCE(SUM(CASE WHEN t.isIncome = 0 THEN t.amount ELSE 0 END), 0) AS spentAmount '
            . 'FROM budgets b '
            . 'INNER JOIN categories c ON c.id = b.categoryId '
            . 'LEFT JOIN transactions t ON t.userId = b.userId AND t.categoryId = b.categoryId AND t.isBusiness = ? AND DATE_FORMAT(t.timestamp, "%Y-%m") = b.month '
            . 'WHERE b.userId = ? AND c.isBusiness = ? '
            . 'AND (? = "" OR b.month = ?) '
            . 'GROUP BY b.id, b.userId, b.categoryId, c.name, b.amount, b.month '
            . 'ORDER BY b.id DESC'
        );
        $stmt->bind_param('iiiss', $isBusiness, $userId, $isBusiness, $monthFilter, $monthFilter);
        $stmt->execute();

        $rows = array_map('normalize_row', fetch_all($stmt));
        json_response(true, 'Budgets loaded', $rows);
    }

    if ($method === 'POST') {
        $categoryId = input_int($input, 'categoryId');
        $amount = input_float($input, 'amount');
        $month = input_string($input, 'month');

        if (!$categoryId || $amount === null || !$month) {
            json_response(false, 'categoryId, amount and month are required', null, 422);
        }

        $stmt = $conn->prepare('INSERT INTO budgets (userId, categoryId, amount, month) VALUES (?, ?, ?, ?)');
        $stmt->bind_param('iids', $userId, $categoryId, $amount, $month);
        $stmt->execute();

        json_response(true, 'Budget created', [
            'id' => $conn->insert_id,
            'userId' => $userId,
            'categoryId' => $categoryId,
            'amount' => $amount,
            'month' => $month,
        ], 201);
    }

    if ($method === 'PUT') {
        $id = input_int($input, 'id');
        $categoryId = input_int($input, 'categoryId');
        $amount = input_float($input, 'amount');
        $month = input_string($input, 'month');

        if (!$id || !$categoryId || $amount === null || !$month) {
            json_response(false, 'id, userId, categoryId, amount and month are required', null, 422);
        }

        $stmt = $conn->prepare('UPDATE budgets SET categoryId = ?, amount = ?, month = ? WHERE id = ? AND userId = ?');
        $stmt->bind_param('idsii', $categoryId, $amount, $month, $id, $userId);
        $stmt->execute();

        json_response(true, 'Budget updated', ['affectedRows' => $stmt->affected_rows]);
    }

    if ($method === 'DELETE') {
        $id = input_int($input, 'id');

        if (!$id) {
            json_response(false, 'id is required', null, 422);
        }

        $stmt = $conn->prepare('DELETE FROM budgets WHERE id = ? AND userId = ?');
        $stmt->bind_param('ii', $id, $userId);
        $stmt->execute();

        json_response(true, 'Budget deleted', ['affectedRows' => $stmt->affected_rows]);
    }

    json_response(false, 'Method not allowed', null, 405);
} catch (Throwable $e) {
    json_response(false, 'Server error: ' . $e->getMessage(), null, 500);
}
