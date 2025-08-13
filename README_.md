# Mini Library Management System (Java, OOP)

## 📌 Overview
The **Mini Library Management System** is a Java-based desktop application built with **Swing GUI** and **OOP principles**.  
It provides an intuitive interface for managing books, members, and transactions in a small to medium-sized library.  
Data is stored in **CSV files** for persistence.

## ✨ Features
- **Book Management**
  - Add, delete, and view books
  - Store title, author, and availability
- **Member Management**
  - Register new members
  - Maintain member records
- **Transaction Management**
  - Issue and return books
  - Log all transactions with timestamps
- **Persistent Storage**
  - Data stored in `books.csv`, `members.csv`, `transactions.csv`
- **OOP Concepts**
  - Encapsulation, Inheritance, and Polymorphism for clean maintainable code

## 📂 Project Structure
```
MiniLibraryApp.java        # Main Java application (Swing GUI)
books.csv                  # Book database
members.csv                # Member database
transactions.csv           # Transaction logs
```

## 📄 CSV File Formats

### books.csv
```
BookID,Title,Author,Available
1,The Great Gatsby,F. Scott Fitzgerald,true
2,1984,George Orwell,true
3,To Kill a Mockingbird,Harper Lee,false
```

### members.csv
```
MemberID,Name,Email
1,John Doe,john@example.com
2,Jane Smith,jane@example.com
```

### transactions.csv
```
TransactionID,BookID,MemberID,Action,Date
1,1,1,ISSUE,2025-08-13
2,1,1,RETURN,2025-08-20
```

## ▶️ How to Run

1. **Install Java (JDK)** on your system.
2. Place `MiniLibraryApp.java` and all `.csv` files in the same folder.
3. Compile the application:
   ```bash
   javac MiniLibraryApp.java
   ```
4. Run the application:
   ```bash
   java MiniLibraryApp
   ```

## 📊 Tech Stack
- **Language:** Java
- **GUI Framework:** Swing
- **Data Storage:** CSV files
- **Paradigm:** Object-Oriented Programming (OOP)

## 🚀 Future Enhancements
- Add search and filter options
- Generate PDF receipts for transactions
- Implement overdue book reminders
- Switch from CSV to database (MySQL / SQLite)

---
Developed as a **portfolio project** to demonstrate Java OOP, GUI development, and file handling.
