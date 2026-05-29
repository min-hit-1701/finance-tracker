<?php
class Database
{
    private string $host;
    private string $db_name;
    private string $username;
    private string $password;
    private ?mysqli $conn = null;

    public function __construct()
    {
        $this->host = getenv('DB_HOST') ?: 'localhost';
        $this->db_name = getenv('DB_NAME') ?: 'finance_tracker_db';
        $this->username = getenv('DB_USERNAME') ?: 'root';
        $this->password = getenv('DB_PASSWORD') ?: '';
    }

    public function connect(): mysqli
    {
        if ($this->conn instanceof mysqli) {
            return $this->conn;
        }

        mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);

        $this->conn = new mysqli($this->host, $this->username, $this->password, $this->db_name);
        $this->conn->set_charset('utf8mb4');

        return $this->conn;
    }
}
