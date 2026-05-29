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

function ensure_business_payments_table(mysqli $conn): void
{
    $conn->query(
        'CREATE TABLE IF NOT EXISTS business_payments ('
        . 'id INT AUTO_INCREMENT PRIMARY KEY, '
        . 'userId INT NOT NULL, '
        . 'receiver VARCHAR(255) NOT NULL, '
        . 'account VARCHAR(120) NOT NULL, '
        . 'amount DECIMAL(15,2) NOT NULL, '
        . 'note TEXT NULL, '
        . 'status VARCHAR(30) NOT NULL DEFAULT "completed", '
        . 'transactionId INT NULL, '
        . 'createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, '
        . 'INDEX idx_business_payments_user (userId), '
        . 'INDEX idx_business_payments_transaction (transactionId)'
        . ') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci'
    );
}

try {
    $conn = (new Database())->connect();
    ensure_business_payments_table($conn);

    if ($method === 'GET') {
        $stmt = $conn->prepare('SELECT id, userId, receiver, account, amount, note, status, transactionId, createdAt FROM business_payments WHERE userId = ? ORDER BY id DESC');
        $stmt->bind_param('i', $userId);
        $stmt->execute();

        json_response(true, 'Payments loaded', array_map('normalize_row', fetch_all($stmt)));
    }

    if ($method === 'POST') {
        $receiver = input_string($input, 'receiver');
        $account = input_string($input, 'account');
        $amount = input_float($input, 'amount');
        $note = input_string($input, 'note', '');
        $walletId = input_int($input, 'walletId');
        $categoryId = input_int($input, 'categoryId');
        $timestamp = input_string($input, 'timestamp', date('Y-m-d H:i:s'));

        if (!$receiver || !$account || $amount === null || $amount <= 0) {
            json_response(false, 'receiver, account and positive amount are required', null, 422);
        }

        $conn->begin_transaction();

        $transactionId = null;
        if ($walletId && $categoryId) {
            $isIncome = 0;
            $paymentNote = $note ?: 'Business payment to ' . $receiver;

            $stmt = $conn->prepare('INSERT INTO transactions (userId, amount, timestamp, note, categoryId, walletId, isIncome, isBusiness) VALUES (?, ?, ?, ?, ?, ?, ?, 1)');
            $stmt->bind_param('idssiii', $userId, $amount, $timestamp, $paymentNote, $categoryId, $walletId, $isIncome);
            $stmt->execute();
            $transactionId = $conn->insert_id;

            $signedAmount = -$amount;
            $stmt = $conn->prepare('UPDATE wallets SET balance = balance + ? WHERE id = ? AND userId = ? AND isBusiness = 1');
            $stmt->bind_param('dii', $signedAmount, $walletId, $userId);
            $stmt->execute();
        }

        $status = 'completed';
        $stmt = $conn->prepare('INSERT INTO business_payments (userId, receiver, account, amount, note, status, transactionId) VALUES (?, ?, ?, ?, ?, ?, ?)');
        $stmt->bind_param('issdssi', $userId, $receiver, $account, $amount, $note, $status, $transactionId);
        $stmt->execute();
        $id = $conn->insert_id;

        $conn->commit();

        json_response(true, 'Payment created', [
            'id' => $id,
            'userId' => $userId,
            'receiver' => $receiver,
            'account' => $account,
            'amount' => $amount,
            'note' => $note,
            'status' => $status,
            'transactionId' => $transactionId,
        ], 201);
    }

    if ($method === 'DELETE') {
        $id = input_int($input, 'id');

        if (!$id) {
            json_response(false, 'id is required', null, 422);
        }

        $stmt = $conn->prepare('DELETE FROM business_payments WHERE id = ? AND userId = ?');
        $stmt->bind_param('ii', $id, $userId);
        $stmt->execute();

        json_response(true, 'Payment deleted', ['affectedRows' => $stmt->affected_rows]);
    }

    json_response(false, 'Method not allowed', null, 405);
} catch (Throwable $e) {
    if (isset($conn) && $conn instanceof mysqli) {
        $conn->rollback();
    }
    json_response(false, 'Server error: ' . $e->getMessage(), null, 500);
}
