# Finance Tracker PHP API

API nay dung MySQL theo dung ten bang/cot trong `DATABASE.sql`: `users`, `wallets`, `categories`, `transactions`, `budgets`, `fullName`, `userId`, `isIncome`, `isBusiness`.

## Cau hinh database

Mac dinh:

```text
DB_HOST=localhost
DB_NAME=finance_tracker_db
DB_USERNAME=root
DB_PASSWORD=
```

Co the doi truc tiep trong `config/database.php` hoac set bien moi truong tren server.

## Nhom dang nhap / dang ky

```text
POST /backend/auth/register.php
Body: { "fullName": "Nguyen Van A", "email": "a@example.com", "password": "123456" }

POST /backend/auth/login.php
Body: { "email": "a@example.com", "password": "123456" }
```

## Nhom personal

Tat ca endpoint personal tu dong loc `isBusiness = 0`.

```text
GET    /backend/personal/wallets.php?userId=1
POST   /backend/personal/wallets.php
PUT    /backend/personal/wallets.php
DELETE /backend/personal/wallets.php

GET    /backend/personal/categories.php?userId=1
POST   /backend/personal/categories.php
PUT    /backend/personal/categories.php
DELETE /backend/personal/categories.php

GET    /backend/personal/transactions.php?userId=1
POST   /backend/personal/transactions.php
DELETE /backend/personal/transactions.php

GET    /backend/personal/budgets.php?userId=1&month=2026-05
POST   /backend/personal/budgets.php
PUT    /backend/personal/budgets.php
DELETE /backend/personal/budgets.php

GET    /backend/personal/summary.php?userId=1
```

## Alias API theo Fragment personal

Neu muon dat API gan voi tung man hinh Fragment, co the goi cac file sau:

```text
AddWalletFragment          POST   /backend/personal/add_wallet.php
WalletFragment             GET    /backend/personal/wallets.php
AddCategoryFragment        POST   /backend/personal/add_category.php
CategoryFragment           GET    /backend/personal/categories.php
AddBudgetFragment          POST   /backend/personal/add_budget.php
BudgetFragment             GET    /backend/personal/budgets.php
AddTransactionFragment     POST   /backend/personal/add_transaction.php
HomeFragment               GET    /backend/personal/home.php
ReportFragment             GET    /backend/personal/report.php
```

## Nhom business

Tat ca endpoint business tu dong loc `isBusiness = 1`.

```text
GET    /backend/business/wallets.php?userId=1
POST   /backend/business/wallets.php
PUT    /backend/business/wallets.php
DELETE /backend/business/wallets.php

GET    /backend/business/categories.php?userId=1
POST   /backend/business/categories.php
PUT    /backend/business/categories.php
DELETE /backend/business/categories.php

GET    /backend/business/transactions.php?userId=1
POST   /backend/business/transactions.php
DELETE /backend/business/transactions.php

GET    /backend/business/budgets.php?userId=1&month=2026-05
POST   /backend/business/budgets.php
PUT    /backend/business/budgets.php
DELETE /backend/business/budgets.php

GET    /backend/business/summary.php?userId=1

GET    /backend/business/dashboard.php?userId=1&month=2026-05
GET    /backend/business/report.php?userId=1&month=2026-05

GET    /backend/business/entities.php?userId=1
GET    /backend/business/entities.php?userId=1&id=1
POST   /backend/business/entities.php
PUT    /backend/business/entities.php
DELETE /backend/business/entities.php

GET    /backend/business/detail.php?userId=1&id=1

GET    /backend/business/payments.php?userId=1
POST   /backend/business/payments.php
DELETE /backend/business/payments.php
```

## Mapping man hinh business

```text
BusinessFragment              -> /backend/business/entities.php
BusinessDetailFragment        -> /backend/business/detail.php?id=...
DashboardFragment             -> /backend/business/dashboard.php
ReportFragment                -> /backend/business/report.php
BusinessWalletFragment        -> /backend/business/wallets.php
BusinessAddWalletFragment     -> POST /backend/business/wallets.php
TransactionFragment           -> /backend/business/transactions.php
BusinessAddTransactionFragment-> POST /backend/business/transactions.php
BusinessBudgetFragment        -> /backend/business/budgets.php
BusinessAddBudgetFragment     -> POST /backend/business/budgets.php
BusinessPaymentFragment       -> /backend/business/payments.php
```

## Body mau

Tao wallet:

```json
{
  "userId": 1,
  "name": "Vi tien mat",
  "balance": 1000000,
  "type": "Cash"
}
```

Tao category:

```json
{
  "userId": 1,
  "name": "An uong",
  "iconRes": "icon_food",
  "colorHex": "#FF5733",
  "isIncome": 0
}
```

Tao transaction:

```json
{
  "userId": 1,
  "amount": 50000,
  "timestamp": "2026-05-28 20:00:00",
  "note": "Com trua",
  "categoryId": 1,
  "walletId": 8,
  "isIncome": 0
}
```

Tao budget:

```json
{
  "userId": 1,
  "categoryId": 1,
  "amount": 3000000,
  "month": "2026-05"
}
```

Tao business entity:

```json
{
  "userId": 1,
  "name": "Cua hang Quan 1",
  "type": "Ban le",
  "note": "Chi nhanh chinh"
}
```

Tao payment:

```json
{
  "userId": 1,
  "receiver": "Nha cung cap A",
  "account": "0123456789",
  "amount": 2500000,
  "note": "Thanh toan tien hang",
  "walletId": 8,
  "categoryId": 3
}
```
