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
        $stmt = $conn->prepare('SELECT id, userId, name, balance, type, isBusiness FROM wallets WHERE userId = ? AND isBusiness = ? ORDER BY id DESC');
        $stmt->bind_param('ii', $userId, $isBusiness);
        $stmt->execute();
        $rows = array_map('normalize_row', fetch_all($stmt));
        json_response(true, 'Wallets loaded', $rows);
    }

    if ($method === 'POST') {
        $name = input_string($input, 'name');
        $balance = input_float($input, 'balance', 0);
        $type = input_string($input, 'type', 'Cash');

        if (!$name) {
            json_response(false, 'name is required', null, 422);
        }

        $stmt = $conn->prepare('INSERT INTO wallets (userId, name, balance, type, isBusiness) VALUES (?, ?, ?, ?, ?)');
        $stmt->bind_param('isdsi', $userId, $name, $balance, $type, $isBusiness);
        $stmt->execute();

        json_response(true, 'Wallet created', [
            'id' => $conn->insert_id,
            'userId' => $userId,
            'name' => $name,
            'balance' => $balance,
            'type' => $type,
            'isBusiness' => $isBusiness,
        ], 201);
    }

    if ($method === 'PUT') {
        $id = input_int($input, 'id');
        $name = input_string($input, 'name');
        $balance = input_float($input, 'balance');
        $type = input_string($input, 'type');

        if (!$id || !$name || $balance === null || !$type) {
            json_response(false, 'id, userId, name, balance and type are required', null, 422);
        }

        $stmt = $conn->prepare('UPDATE wallets SET name = ?, balance = ?, type = ? WHERE id = ? AND userId = ? AND isBusiness = ?');
        $stmt->bind_param('sdsiii', $name, $balance, $type, $id, $userId, $isBusiness);
        $stmt->execute();

        json_response(true, 'Wallet updated', ['affectedRows' => $stmt->affected_rows]);
    }

    if ($method === 'DELETE') {
        $id = input_int($input, 'id');

        if (!$id) {
            json_response(false, 'id is required', null, 422);
        }

        $stmt = $conn->prepare('DELETE FROM wallets WHERE id = ? AND userId = ? AND isBusiness = ?');
        $stmt->bind_param('iii', $id, $userId, $isBusiness);
        $stmt->execute();

        json_response(true, 'Wallet deleted', ['affectedRows' => $stmt->affected_rows]);
    }

    json_response(false, 'Method not allowed', null, 405);
} catch (Throwable $e) {
    json_response(false, 'Server error: ' . $e->getMessage(), null, 500);
}
