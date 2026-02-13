---
name: Android Robustness & Reliability
description: Systematic best practices and tools to prevent runtime issues, race conditions, and corner cases in Android apps.
---

# Android Robustness & Reliability (Best Practices)

This skill outlines the systematic approach and tools for handling non-obvious runtime issues like race conditions, corner cases, and architectural trade-offs in high-quality Android applications.

## 1. Essential Tools

### Static Analysis (Prevention)

- **Detekt**: Go beyond Lint. Use it to enforce complexity limits (Cyclomatic Complexity) and detect "code smells" that lead to corner cases.
- **Android Lint**: Configure custom rules for threading (e.g., `@WorkerThread`, `@UiThread` annotations).
- **Kotlin Coroutines Debugger**: Use `-Dkotlinx.coroutines.debug` to trace coroutine leaks and deadlocks.

### Dynamic Analysis (Detection)

- **StrictMode**: Enable in Debug builds to catch disk/network IO on the main thread and leaked Closables.
- **LeakCanary**: Mandatory for detecting memory leaks in long-running processes (like a clock app).
- **ADB Monkey**: Run `adb shell monkey -p your.package.name 5000` to stress-test UI stability and find unhandled exceptions in rapid interaction sequences.
- **Layout Inspector**: Validate view hierarchies during complex transitions/rotations.

## 2. Best Practices for Race Conditions

### UI State Synchronization

- **Main Thread Serialization**: In Android, the UI thread is naturally serial. Instead of heavy locks, use `Handler(Looper.getMainLooper()).post {}` or `View.post {}` to ensure operations happen in a predictable order.
- **Atomic State Flags**: Use `AtomicBoolean` or `volatile` properties for simple state flags accessed across threads.
- **State Mutual Exclusion**: When multiple controllers can affect the same UI component (e.g., System Time vs. Time Travel), use a "Primary Controller" pattern or an `isActive` flag to explicitly pause/resume conflicting background tasks.

### Coroutines Safety

- **Mutex**: Use `kotlinx.coroutines.sync.Mutex` for non-blocking mutual exclusion in coroutines.
- **Structured Concurrency**: Always bind coroutines to a `LifecycleScope` or `ViewModelScope` to prevent orphaned background tasks from updating a destroyed UI.

## 3. Handling Corner Cases

### Defensive Lifecycle Management

- **Tokenized Callbacks**: Use a "request token" or check `isInitialized` before executing late-arrival callbacks (e.g., network results or delayed animations).
- **Graceful Degradation**: If a hardware feature (like a specific vibrator type or display refresh rate) isn't available, have a fallback. Never assume hardware availability.

### The "Rotation Flash" and Configuration Changes

- **Atomic Re-initialization**: When the screen rotates, treat the restoration of state as an atomic transaction. Order of operations: `Restore State` -> `Re-bind View` -> `Resume Animations`.
- **Pre-emptive Teardown**: Explicitly cancel all hardware observers (e.g., `DisplayListener`, `WakeLock`) in `onPause` or `onDestroy` to avoid "zombie" updates to a recycled activity.

## 4. Documenting Trade-offs

- **"Why" Over "What"**: When choosing between speed and consistency (e.g., using a cached value vs. re-reading from disk), document the decision in the code using `// Trade-off: [Reason]`.
- **Performance Budgeting**: For 60fps UI, consistency checks MUST be O(1). If a check is O(N), move it to a worker thread and accept a small "consistency lag" (Eventual Consistency).

## 5. Verification Checklist

- [ ] Does the app survive 1000 random events from ADB Monkey?
- [ ] Are all `BroadcastReceivers` and `Listeners` unregistered in the companion lifecycle method?
- [ ] Is there any shared state that can be modified by two different controllers simultaneously?
- [ ] Does the UI state remain consistent after 10 rapid screen rotations?
