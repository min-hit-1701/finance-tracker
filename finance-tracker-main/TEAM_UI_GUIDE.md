# TEAM UI + WORKFLOW GUIDE - FINANCE TRACKER

Tai lieu nay la bo quy uoc dung chung cho ca nhom:
- UI system (mau sac, typography, component)
- Quy tac dat ten file
- Chia module de code song song, it xung dot merge

## 1) App flow tong quan

```text
Splash
  -> Check Login
      -> Chua login: Login -> Register
      -> Da login: Check mode
          -> Personal Main
          -> Business Main
```

Mode duoc luu trong `SharedPreferences`.

## 2) Phan chia 5 thanh vien (module ownership)

1. Core/Auth/Navigation
   - Splash, Login, Register, ModeSelect
   - `MainActivity` (personal), `BusinessMainActivity` (business)
   - Bottom navigation + fragment container

2. Personal Home + Transaction
   - `HomeFragment`
   - `AddEditTransactionFragment`
   - `TransactionDetailFragment`

3. Personal Wallet + Category
   - `WalletFragment`
   - `CategoryFragment`

4. Personal Budget + Report
   - `BudgetFragment`
   - `ReportFragment`

5. Business Module (Minh)
   - `DashboardFragment`
   - `BusinessFragment`
   - `TransactionFragment` (business)
   - `ReportFragment` (business)

Rule: moi nguoi ownership 1 cum file, khong sua XML/Fragment cua nguoi khac neu chua thong nhat.

## 3) Naming conventions (bat buoc)

- Fragment layout: `fragment_<feature>.xml`
- Item layout: `item_<feature>.xml`
- Activity layout: `activity_<feature>.xml`
- Menu: `bottom_nav_menu.xml`, `business_bottom_nav_menu.xml`

Vi du dung:
- `fragment_home.xml`
- `fragment_wallet.xml`

Vi du sai:
- `home_screen.xml`
- `walletUI.xml`

## 4) Shared color system

Dung token trong `app/src/main/res/values/colors.xml`:

- `brand_primary`: `#2F54FF`
- `brand_primary_dark`: `#1E3DD6`
- `brand_primary_soft`: `#E8EDFF`
- `bg_app`: `#F6F7FF`
- `bg_surface`: `#FFFFFF`
- `text_primary`: `#111827`
- `text_secondary`: `#6B7280`
- `text_hint`: `#9CA3AF`
- `stroke_default`: `#E5E7EB`
- `success`: `#22C55E`
- `warning`: `#F59E0B`
- `error`: `#EF4444`
- `income_green`: `#16A34A`
- `expense_red`: `#DC2626`

Khong hardcode mau trong layout neu da co token.

## 5) Shared typography + component styles

Da khai bao tai `app/src/main/res/values/themes.xml`:

- Text styles:
  - `@style/Text.Finance.Title`
  - `@style/Text.Finance.Heading`
  - `@style/Text.Finance.Subheading`
  - `@style/Text.Finance.Body`
  - `@style/Text.Finance.Caption`

- Component styles:
  - `@style/Widget.Finance.Button.Primary`
  - `@style/Widget.Finance.Button.Secondary`
  - `@style/Widget.Finance.Card`
  - `@style/Widget.Finance.Input`

## 6) Spacing + sizing standards

- Button height: `48dp`
- Button radius: `24dp`
- Card radius: `16dp`
- Screen horizontal padding: `16dp`
- Section spacing: `16dp`
- Small item spacing: `8dp`
- Icon size: `20dp`-`24dp`

## 7) Fake data first (de chot UI nhanh)

Giai doan dau chi can fake data:
- `List<Transaction>`
- `List<Wallet>`
- `List<Category>`
- `List<Business>`

Chua lam DB/Room ngay khi UI chua on dinh.

## 8) De xuat timeline 5 ngay

- Ngay 1-2: core navigation + tat ca layout XML
- Ngay 3-4: gan fragment, mock data, fix UI
- Ngay 5: polish, test flow, demo

## 9) Merge checklist

- Dung token mau va style chung
- Text dua vao `strings.xml`, khong hardcode
- Man hinh khong vo bo cuc tren may nho
- Build debug pass truoc khi merge
- Dat ten file dung convention
