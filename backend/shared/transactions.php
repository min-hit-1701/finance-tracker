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

function ensure_transactions_icon_column(mysqli $conn): void
{
    $result = $conn->query("SHOW COLUMNS FROM transactions LIKE 'iconRes'");
    if ($result->num_rows === 0) {
        $conn->query('ALTER TABLE transactions ADD COLUMN iconRes VARCHAR(60) NULL AFTER isBusiness');
    }
}

try {
    $conn = (new Database())->connect();
    ensure_transactions_icon_column($conn);

    if ($method === 'GET') {
        $categoryId = input_int($input, 'categoryId');
        $walletId = input_int($input, 'walletId');

        $categoryFilter = $categoryId ?: 0;
        $walletFilter = $walletId ?: 0;

        $stmt = $conn->prepare(
            'SELECT id, userId, amount, timestamp, note, categoryId, walletId, isIncome, isBusiness, iconRes '
            . 'FROM transactions '
            . 'WHERE userId = ? AND isBusiness = ? '
            . 'AND (? = 0 OR categoryId = ?) '
            . 'AND (? = 0 OR walletId = ?) '
            . 'ORDER BY timestamp DESC'
        );
        $stmt->bind_param('iiiiii', $userId, $isBusiness, $categoryFilter, $categoryFilter, $walletFilter, $walletFilter);
        $stmt->execute();

        $rows = array_map('normalize_row', fetch_all($stmt));
        json_response(true, 'Transactions loaded', $rows);
    }

    if ($method === 'POST') {
        $amount = input_float($input, 'amount');
        $timestamp = input_string($input, 'timestamp', date('Y-m-d H:i:s'));
        $note = input_string($input, 'note');
        $categoryId = input_int($input, 'categoryId');
        $walletId = input_int($input, 'walletId');
        $isIncome = input_bool($input, 'isIncome');
        $iconRes = input_string($input, 'iconRes');

        if ($amount === null || !$categoryId || !$walletId || $isIncome === null) {
            json_response(false, 'amount, categoryId, walletId and isIncome are required', null, 422);
        }

        $incomeValue = $isIncome ? 1 : 0;
        $signedAmount = $incomeValue === 1 ? $amount : -$amount;

        $conn->begin_transaction();

        $stmt = $conn->prepare('INSERT INTO transactions (userId, amount, timestamp, note, categoryId, walletId, isIncome, isBusiness, iconRes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)');
        $stmt->bind_param('idssiiiis', $userId, $amount, $timestamp, $note, $categoryId, $walletId, $incomeValue, $isBusiness, $iconRes);
        $stmt->execute();
        $id = $conn->insert_id;

        $stmt = $conn->prepare('UPDATE wallets SET balance = balance + ? WHERE id = ? AND userId = ? AND isBusiness = ?');
        $stmt->bind_param('diii', $signedAmount, $walletId, $userId, $isBusiness);
        $stmt->execute();

        $conn->commit();

        json_response(true, 'Transaction created', [
            'id' => $id,
            'userId' => $userId,
            'amount' => $amount,
            'timestamp' => $timestamp,
            'note' => $note,
            'categoryId' => $categoryId,
            'walletId' => $walletId,
            'isIncome' => $incomeValue,
            'isBusiness' => $isBusiness,
            'iconRes' => $iconRes,
        ], 201);
    }

    if ($method === 'DELETE') {
        $id = input_int($input, 'id');

        if (!$id) {
            json_response(false, 'id is required', null, 422);
        }

        $conn->begin_transaction();

        $stmt = $conn->prepare('SELECT amount, walletId, isIncome FROM transactions WHERE id = ? AND userId = ? AND isBusiness = ? LIMIT 1');
        $stmt->bind_param('iii', $id, $userId, $isBusiness);
        $stmt->execute();
        $transaction = fetch_one($stmt);

        if (!$transaction) {
            $conn->rollback();
            json_response(false, 'Transaction not found', null, 404);
        }

        $signedAmount = (int) $transaction['isIncome'] === 1 ? -(float) $transaction['amount'] : (float) $transaction['amount'];
        $walletId = (int) $transaction['walletId'];

        $stmt = $conn->prepare('DELETE FROM transactions WHERE id = ? AND userId = ? AND isBusiness = ?');
        $stmt->bind_param('iii', $id, $userId, $isBusiness);
        $stmt->execute();

        $stmt = $conn->prepare('UPDATE wallets SET balance = balance + ? WHERE id = ? AND userId = ? AND isBusiness = ?');
        $stmt->bind_param('diii', $signedAmount, $walletId, $userId, $isBusiness);
        $stmt->execute();

        $conn->commit();

        json_response(true, 'Transaction deleted', ['id' => $id]);
    }

    json_response(false, 'Method not allowed', null, 405);
} catch (Throwable $e) {
    if (isset($conn) && $conn instanceof mysqli) {
        $conn->rollback();
    }
    json_response(false, 'Server error: ' . $e->getMessage(), null, 500);
}
