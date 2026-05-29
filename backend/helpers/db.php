<?php
function fetch_all(mysqli_stmt $stmt): array
{
    $result = $stmt->get_result();
    return $result ? $result->fetch_all(MYSQLI_ASSOC) : [];
}

function fetch_one(mysqli_stmt $stmt): ?array
{
    $result = $stmt->get_result();
    $row = $result ? $result->fetch_assoc() : null;
    return $row ?: null;
}

function normalize_row(array $row): array
{
    foreach ($row as $key => $value) {
        if (in_array($key, ['id', 'userId', 'categoryId', 'walletId', 'isIncome', 'isBusiness', 'transactionId'], true) && $value !== null) {
            $row[$key] = (int) $value;
        } elseif (in_array($key, ['amount', 'balance', 'total', 'income', 'expense', 'spentAmount', 'totalBalance', 'totalIncome', 'totalExpense', 'budgetAmount', 'percent'], true) && $value !== null) {
            $row[$key] = (float) $value;
        }
    }

    return $row;
}
