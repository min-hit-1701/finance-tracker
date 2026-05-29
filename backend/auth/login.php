<?php
require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/request.php';
require_once __DIR__ . '/../helpers/db.php';
require_once __DIR__ . '/../helpers/auth.php';

allow_cors('POST, OPTIONS');
require_method(['POST']);

$data = request_data();
$username = input_string($data, 'username', input_string($data, 'email', ''));
$password = input_string($data, 'password', '');

if ($username === '' || $password === '') {
    json_response(false, 'Vui lòng nhập tên người dùng/email và mật khẩu', null, 422);
}

try {
    $db = (new Database())->connect();
    $stmt = $db->prepare('SELECT id, fullName, email, password FROM users WHERE email = ? OR fullName = ? LIMIT 1');
    $stmt->bind_param('ss', $username, $username);
    $stmt->execute();
    $user = fetch_one($stmt);

    if (!$user || !hash_equals((string) $user['password'], $password)) {
        json_response(false, 'Đăng nhập sai mật khẩu hoặc tên người dùng', null, 401);
    }

    unset($user['password']);
    $user = normalize_row($user);

    json_response(true, 'Đăng nhập thành công', [
        'user' => $user,
        'token' => issue_token($user),
    ]);
} catch (Throwable $e) {
    json_response(false, 'Không thể đăng nhập. Vui lòng thử lại', null, 500);
}
