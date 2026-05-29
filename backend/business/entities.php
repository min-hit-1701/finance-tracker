<?php
require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/request.php';
require_once __DIR__ . '/../helpers/db.php';
require_once __DIR__ . '/../helpers/auth.php';

allow_cors();

$input = request_data();
$method = $_SERVER['REQUEST_METHOD'];
$authUser = require_auth_user($input);
$userId = (int) $authUser['id'];

function ensure_business_entities_table(mysqli $conn): void
{
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
}

try {
    $conn = (new Database())->connect();
    ensure_business_entities_table($conn);

    if ($method === 'GET') {
        $id = input_int($input, 'id');

        if ($id) {
            $stmt = $conn->prepare('SELECT id, userId, name, type, note, createdAt, updatedAt FROM business_entities WHERE id = ? AND userId = ? LIMIT 1');
            $stmt->bind_param('ii', $id, $userId);
            $stmt->execute();
            $row = fetch_one($stmt);

            if (!$row) {
                json_response(false, 'Business entity not found', null, 404);
            }

            json_response(true, 'Business entity loaded', normalize_row($row));
        }

        $stmt = $conn->prepare('SELECT id, userId, name, type, note, createdAt, updatedAt FROM business_entities WHERE userId = ? ORDER BY id DESC');
        $stmt->bind_param('i', $userId);
        $stmt->execute();

        json_response(true, 'Business entities loaded', array_map('normalize_row', fetch_all($stmt)));
    }

    if ($method === 'POST') {
        $name = input_string($input, 'name');
        $type = input_string($input, 'type');
        $note = input_string($input, 'note', '');

        if (!$name || !$type) {
            json_response(false, 'name and type are required', null, 422);
        }

        $stmt = $conn->prepare('INSERT INTO business_entities (userId, name, type, note) VALUES (?, ?, ?, ?)');
        $stmt->bind_param('isss', $userId, $name, $type, $note);
        $stmt->execute();

        json_response(true, 'Business entity created', [
            'id' => $conn->insert_id,
            'userId' => $userId,
            'name' => $name,
            'type' => $type,
            'note' => $note,
        ], 201);
    }

    if ($method === 'PUT') {
        $id = input_int($input, 'id');
        $name = input_string($input, 'name');
        $type = input_string($input, 'type');
        $note = input_string($input, 'note', '');

        if (!$id || !$name || !$type) {
            json_response(false, 'id, name and type are required', null, 422);
        }

        $stmt = $conn->prepare('UPDATE business_entities SET name = ?, type = ?, note = ? WHERE id = ? AND userId = ?');
        $stmt->bind_param('sssii', $name, $type, $note, $id, $userId);
        $stmt->execute();

        json_response(true, 'Business entity updated', ['affectedRows' => $stmt->affected_rows]);
    }

    if ($method === 'DELETE') {
        $id = input_int($input, 'id');

        if (!$id) {
            json_response(false, 'id is required', null, 422);
        }

        $stmt = $conn->prepare('DELETE FROM business_entities WHERE id = ? AND userId = ?');
        $stmt->bind_param('ii', $id, $userId);
        $stmt->execute();

        json_response(true, 'Business entity deleted', ['affectedRows' => $stmt->affected_rows]);
    }

    json_response(false, 'Method not allowed', null, 405);
} catch (Throwable $e) {
    json_response(false, 'Server error: ' . $e->getMessage(), null, 500);
}
