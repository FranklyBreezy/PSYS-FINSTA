# PSYS-FINSTA (Offline & Privacy-Centric)

A lightweight, offline-first personal finance tracker built with **Java** and **Android Jetpack** components. Designed for performance, minimal resource usage, and complete data privacy — with zero cloud dependencies.

---

## 🚀 Features

- ✅ **Track income and expenses**  
- ✅ **Offline-only**: No internet required, full local storage  
- ✅ **Room DB** with planned **data encryption**  
- ✅ **MVVM Architecture** (Model–View–ViewModel)  
- ✅ **Lazy loading** for memory-efficient rendering  
- ✅ **Manual Dependency Injection** (for testability and loose coupling)  
- ✅ **Expandable architecture** for future cloud sync (optional)  

---

## 🛠️ Tech Stack

| Layer               | Implementation                          |
|---------------------|----------------------------------------|
| Language            | Java                                   |
| Architecture        | MVVM                                   |
| Database            | Room                                   |
| UI Components       | RecyclerView, ViewModel, LiveData      |
| Dependency Injection| Manual (Constructor Injection)         |
| Build System        | Gradle (Groovy DSL)                    |
| Min SDK             | 26                                     |
| Target SDK          | 34                                     |

---

## 🔐 Privacy & Security

- No data is uploaded or synced to any server  
- Room DB storage is **encrypted at rest** *(planned)*  
- No third-party analytics or tracking libraries  

---

## 📦 Project Structure

```text
com.example.psysfinsta
│
├── data              # Room Entities, DAO, Database
├── repository        # Abstraction over data sources
├── ui
│   ├── view          # Activities / Fragments
│   └── viewmodel     # ViewModels
├── di                # Dependency injection setup
└── utils             # Helper classes
