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
        $isIncome = input_bool($input, 'isIncome');

        $sql = 'SELECT id, userId, name, iconRes, colorHex, isIncome, isBusiness FROM categories '
            . 'WHERE (userId IS NULL OR userId = ?) AND isBusiness = ? ';
        if ($isIncome !== null) {
            $sql .= 'AND isIncome = ? ';
        }
        $sql .= 'ORDER BY name ASC';

        $stmt = $conn->prepare($sql);
        if ($isIncome !== null) {
            $incomeValue = $isIncome ? 1 : 0;
            $stmt->bind_param('iii', $userId, $isBusiness, $incomeValue);
        } else {
            $stmt->bind_param('ii', $userId, $isBusiness);
        }
        $stmt->execute();

        $rows = array_map('normalize_row', fetch_all($stmt));
        json_response(true, 'Categories loaded', $rows);
    }

    if ($method === 'POST') {
        $name = input_string($input, 'name');
        $iconRes = input_string($input, 'iconRes');
        $colorHex = input_string($input, 'colorHex');
        $isIncome = input_bool($input, 'isIncome', false) ? 1 : 0;

        if (!$name) {
            json_response(false, 'name is required', null, 422);
        }

        $stmt = $conn->prepare('INSERT INTO categories (userId, name, iconRes, colorHex, isIncome, isBusiness) VALUES (?, ?, ?, ?, ?, ?)');
        $stmt->bind_param('isssii', $userId, $name, $iconRes, $colorHex, $isIncome, $isBusiness);
        $stmt->execute();

        json_response(true, 'Category created', [
            'id' => $conn->insert_id,
            'userId' => $userId,
            'name' => $name,
            'iconRes' => $iconRes,
            'colorHex' => $colorHex,
            'isIncome' => $isIncome,
            'isBusiness' => $isBusiness,
        ], 201);
    }

    if ($method === 'PUT') {
        $id = input_int($input, 'id');
        $name = input_string($input, 'name');
        $iconRes = input_string($input, 'iconRes');
        $colorHex = input_string($input, 'colorHex');
        $isIncome = input_bool($input, 'isIncome');

        if (!$id || !$name || $isIncome === null) {
            json_response(false, 'id, userId, name and isIncome are required', null, 422);
        }

        $incomeValue = $isIncome ? 1 : 0;
        $stmt = $conn->prepare('UPDATE categories SET name = ?, iconRes = ?, colorHex = ?, isIncome = ? WHERE id = ? AND userId = ? AND isBusiness = ?');
        $stmt->bind_param('sssiiii', $name, $iconRes, $colorHex, $incomeValue, $id, $userId, $isBusiness);
        $stmt->execute();

        json_response(true, 'Category updated', ['affectedRows' => $stmt->affected_rows]);
    }

    if ($method === 'DELETE') {
        $id = input_int($input, 'id');

        if (!$id) {
            json_response(false, 'id is required', null, 422);
        }

        $stmt = $conn->prepare('DELETE FROM categories WHERE id = ? AND userId = ? AND isBusiness = ?');
        $stmt->bind_param('iii', $id, $userId, $isBusiness);
        $stmt->execute();

        json_response(true, 'Category deleted', ['affectedRows' => $stmt->affected_rows]);
    }

    json_response(false, 'Method not allowed', null, 405);
} catch (Throwable $e) {
    json_response(false, 'Server error: ' . $e->getMessage(), null, 500);
}
