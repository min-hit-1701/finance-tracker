<?php
function allow_cors(string $methods = 'GET, POST, PUT, DELETE, OPTIONS'): void
{
    header('Content-Type: application/json; charset=utf-8');
    header('Access-Control-Allow-Origin: *');
    header("Access-Control-Allow-Methods: $methods");
    header('Access-Control-Allow-Headers: Content-Type, Authorization');

    if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
        http_response_code(204);
        exit;
    }
}

function json_response(bool $success, string $message = '', $data = null, int $status = 200): void
{
    http_response_code($status);
    echo json_encode([
        'success' => $success,
        'message' => $message,
        'data' => $data,
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

function require_method(array $methods): void
{
    if (!in_array($_SERVER['REQUEST_METHOD'], $methods, true)) {
        json_response(false, 'Method not allowed', null, 405);
    }
}
