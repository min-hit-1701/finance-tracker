# Firestore Import Tool

Tool Java nay import du lieu tu `DATABASE.sql` vao Firestore theo dung cau truc Android app dang doc:

- `users/{uid}/personal_wallets`
- `users/{uid}/personal_categories`
- `users/{uid}/personal_transactions`
- `users/{uid}/personal_budgets`
- `users/{uid}/business_wallets`
- `users/{uid}/business_categories`
- `users/{uid}/business_transactions`

Tool cung tao Firebase Authentication users tu bang `users` trong SQL. Vi password trong SQL qua ngan cho Firebase, cac tai khoan import se dung password mac dinh:

```text
123456
```

## Cach Chay

Mo terminal tai thu muc goc project va chay:

```powershell
.\gradlew.bat -p tools\firestore-import run
```

Can co 2 file trong thu muc nay:

- `DATABASE.sql`
- `serviceAccountKey.json`

`serviceAccountKey.json` la private key, chi de chay tren may dev. Khong dua file nay vao Android app va khong commit len Git.
