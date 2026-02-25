# CodeMaster 🚀

CodeMaster is a Spring Boot based Code Analysis Tool that analyzes Java source code files and extracts useful structural metrics.

## 🔍 Features
- Analyze Java source files
- Calculate Lines of Code (LOC)
- Count number of classes
- Count number of methods
- REST API based architecture

## 🛠 Tech Stack
- Java
- Spring Boot
- Maven
- MySQL (if used)
- Postman (for API testing)

## 🏗 Architecture
- Controller Layer
- Service Layer
- Repository Layer
- DTO / Model Classes

## ▶️ How to Run
1. Clone the repository
2. Open in Spring Tool Suite (STS) or VS Code
3. Configure application.properties
4. Run as Spring Boot Application
5. Test APIs using Postman

## 📌 Sample API Endpoint
POST /analyze

Input: Java source code file  
Output: LOC count, class count, method count

## 📈 Future Improvements
- Cyclomatic Complexity Calculation
- Code Smell Detection
- UI Dashboard Integration
