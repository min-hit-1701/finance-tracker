<?php
class Database {
    private $host = "192.168.3.2:8081";
    private $db_name = "finance_tracker_db";
    private $username = "root";
    private $password = "";
    private $conn;

    public function connect() {
        $this->conn = null;

        try {
            $this->conn = new mysqli($this->host, $this->username, $this->password, $this->db_name);
            
            if ($this->conn->connect_error) {
                throw new Exception("Connection failed: " . $this->conn->connect_error);
            }
            
            $this->conn->set_charset("utf8mb4");
        } catch (Exception $e) {
            throw new Exception("Database error: " . $e->getMessage());
        }

        return $this->conn;
    }
}
?>
