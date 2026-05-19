<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");

require_once __DIR__ . '/../config/database.php';

$response = [
    'success' => false,
    'message' => '',
    'data' => null
];

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    $response['message'] = 'Method not allowed';
    echo json_encode($response);
    exit;
}

$input = json_decode(file_get_contents('php://input'), true);
if (!$input) {
    $input = $_POST;
}

$username = isset($input['username']) ? trim($input['username']) : '';
$password = isset($input['password']) ? $input['password'] : '';

if (empty($username) || empty($password)) {
    $response['message'] = 'Username and password are required';
    echo json_encode($response);
    exit;
}

try {
    $database = new Database();
    $conn = $database->connect();

    $stmt = $conn->prepare("SELECT id, username, password, email, full_name FROM users WHERE username = ? LIMIT 1");
    $stmt->bind_param("s", $username);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 0) {
        $response['message'] = 'Invalid username or password';
        echo json_encode($response);
        exit;
    }

    $user = $result->fetch_assoc();

    if (!password_verify($password, $user['password'])) {
        $response['message'] = 'Invalid username or password';
        echo json_encode($response);
        exit;
    }

    unset($user['password']);

    $token = bin2hex(random_bytes(32));

    $stmt = $conn->prepare("UPDATE users SET auth_token = ?, token_expires = DATE_ADD(NOW(), INTERVAL 24 HOUR) WHERE id = ?");
    $stmt->bind_param("si", $token, $user['id']);
    $stmt->execute();

    $response['success'] = true;
    $response['message'] = 'Login successful';
    $response['data'] = [
        'user' => $user,
        'token' => $token
    ];

} catch (Exception $e) {
    $response['message'] = 'Server error: ' . $e->getMessage();
}

echo json_encode($response);
?>
