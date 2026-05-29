<?php
require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/request.php';
require_once __DIR__ . '/../helpers/db.php';

allow_cors('POST, OPTIONS');
require_method(['POST']);

$data = request_data();
$fullName = input_string($data, 'fullName', '');
$email = input_string($data, 'email', '');
$password = input_string($data, 'password', '');

if ($fullName === '' || $email === '' || $password === '') {
    json_response(false, 'Vui lòng nhập đầy đủ họ tên, email và mật khẩu', null, 422);
}

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    json_response(false, 'Email không hợp lệ', null, 422);
}

try {
    $db = (new Database())->connect();
    $checkStmt = $db->prepare('SELECT id FROM users WHERE email = ? LIMIT 1');
    $checkStmt->bind_param('s', $email);
    $checkStmt->execute();

    if (fetch_one($checkStmt)) {
        json_response(false, 'Email này đã được đăng ký', null, 409);
    }

    $insertStmt = $db->prepare('INSERT INTO users (fullName, email, password) VALUES (?, ?, ?)');
    $insertStmt->bind_param('sss', $fullName, $email, $password);
    $insertStmt->execute();

    json_response(true, 'Đăng ký thành công. Vui lòng đăng nhập', [
        'id' => $db->insert_id,
        'fullName' => $fullName,
        'email' => $email,
    ], 201);
} catch (Throwable $e) {
    json_response(false, 'Không thể đăng ký. Vui lòng thử lại', null, 500);
}
