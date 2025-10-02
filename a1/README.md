# ECSE-420 Assignment 1

We used gradle to streamline the development of our assignment. The gradle project contains multiple modules separated logically.

It includes:

- **Matrix Multiplication**: Sequential vs parallel matrix multiplication with benchmarking.
- **Dining Philosophers**: Classic concurrency problem with both deadlock-prone and deadlock-free solutions.
- **Deadlock Examples**: Demonstrations of deadlock and resource ordering to prevent it.

---

## 📦 Project Structure

``` bash
a1
├── Makefile
├── deadlock
│   ├── build.gradle.kts
│   └── src
│       └── examples
│           ├── Deadlock.java
│           └── ResourceOrdering.java
├── dining-philosophers
│   ├── build.gradle.kts
│   └── src
│       ├── main
│       │   └── java
│       │       └── solution
│       │           ├── DiningPhilosophers.java
│       │           └── DiningPhilosophersDeadlock.java
│       └── test
│           └── java
│               └── solution
│                   ├── DiningPhilosophersDeadlockTest.java
│                   └── DiningPhilosophersTest.java
├── gradle
│   ├── libs.versions.toml
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradle.properties
├── gradlew
├── gradlew.bat
├── matrix-multiplication
│   ├── build.gradle.kts
│   └── src
│       ├── main
│       │   └── java
│       │       └── solution
│       │           ├── MatrixMultiplication.java
│       │           └── tasks
│       │               └── MatrixMultiplicationTask.java
│       └── test
│           └── java
│               └── solution
│                   └── MatrixMultiplicationTest.java
├── report
│   └── ECSE 420 - A1 Report.pdf
└── settings.gradle.kts

24 directories, 21 files
```

Each subproject is independent and configured with its own `build.gradle.kts`.
The root `Makefile` provides shortcuts for running and testing all projects.

---

## 🚀 Prerequisites

- **Java 21** (configured via Gradle toolchain)
- **Gradle Wrapper** (already included: `./gradlew`; no need to install Gradle manually)
- A UNIX-like shell (Linux/Mac). On Windows, use Git Bash or WSL, or run `gradlew.bat` in PowerShell.

---

## ▶️ Running Programs

The `Makefile` defines commands to run each assignment:

### Matrix Multiplication
```bash
# running matrix multiplication
make run-mmul

# running dining philosophers (with/without starvation or deadlocks)
make run-dphils

# running dining philosophers with deadlock
make run-dphils-deadlock

# running deadlock example
make run-deadlock

# running resource ordering example
make run-rsc-ordering

# running unit tests
make test
```

---

## ⚙️ Alternative: Using Gradle Directly

You can bypass make and run Gradle directly:

``` bash
./gradlew :matrix-multiplication:run
./gradlew :dining-philosophers:run -PmainClass=solution.DiningPhilosophers
./gradlew :dining-philosophers:run -PmainClass=solution.DiningPhilosophersDeadlock
./gradlew :deadlock:run -PmainClass=examples.Deadlock
./gradlew :deadlock:run -PmainClass=examples.ResourceOrdering
./gradlew clean test
```

---

## 📚 Notes
- All concurrency programs run indefinitely by design; use Ctrl+C to stop them.
- The matrix multiplication benchmarks may take significant time for larger sizes.
- Deadlock-prone versions may hang as expected—this is intentional.
