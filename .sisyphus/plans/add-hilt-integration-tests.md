# 添加 Hilt 集成测试计划

## 目标
添加 Hilt 集成测试，验证依赖注入绑定是否正确工作，弥补现有单元测试的不足。

## 背景
- 现有 36 个单元测试使用 Fake 对象和手动构造
- 这些测试**不验证** Hilt 的 DI 绑定是否正确
- 需要添加集成测试来验证真实的依赖注入图

## 测试策略

### 1. 创建 Hilt 测试基础设施

**文件**: `app/src/test/java/com/bokehforu/openflip/di/HiltTestApplication.kt`
```kotlin
@HiltAndroidApp
class HiltTestApplication : Application()
```

**文件**: `app/src/test/java/com/bokehforu/openflip/di/HiltTestRunner.kt`
```kotlin
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
```

### 2. 添加 DI 绑定验证测试

**文件**: `app/src/test/java/com/bokehforu/openflip/di/DependencyInjectionTest.kt`

测试内容：
- 验证所有 @Singleton 组件可以正确注入
- 验证 ViewModel 可以通过 Hilt 创建
- 验证接口绑定（HapticsProvider → HapticFeedbackManager）
- 验证 AssistedInject Factory 可以正常工作

### 3. 添加 Activity 集成测试

**文件**: `app/src/test/java/com/bokehforu/openflip/ui/FullscreenClockActivityTest.kt`

测试内容：
- Activity 启动时所有 @Inject 字段被正确注入
- ViewModel 通过 by viewModels() 正确获取
- 控制器 Factory 可以创建实例

### 4. 添加 ViewModel 集成测试

**文件**: `app/src/test/java/com/bokehforu/openflip/viewmodel/ViewModelInjectionTest.kt`

测试内容：
- FullscreenClockViewModel 可以通过 Hilt 创建
- SavedStateHandle 自动注入
- @ApplicationContext 正确提供

## 需要修改的文件

### build.gradle.kts (app)
添加 Hilt 测试依赖：
```kotlin
testImplementation("com.google.dagger:hilt-android-testing:2.55")
kaptTest("com.google.dagger:hilt-compiler:2.55")
```

### 测试目录结构
```
app/src/test/java/com/bokehforu/openflip/
├── di/
│   ├── HiltTestApplication.kt
│   ├── HiltTestRunner.kt
│   └── DependencyInjectionTest.kt
├── ui/
│   └── FullscreenClockActivityTest.kt
└── viewmodel/
    ├── FullscreenClockViewModelTest.kt (已有，单元测试)
    └── ViewModelInjectionTest.kt (新增，集成测试)
```

## 具体测试用例

### DependencyInjectionTest
1. `singletonComponentsCanBeInjected()` - 验证 AppSettingsManager, HapticFeedbackManager 等
2. `viewModelCanBeCreatedWithHilt()` - 验证 @HiltViewModel 可以实例化
3. `interfaceBindingsWork()` - 验证 HapticsProvider 绑定到 HapticFeedbackManager
4. `assistedInjectFactoryWorks()` - 验证 LightToggleController.Factory 可以创建实例
5. `coroutineScopeIsProvided()` - 验证 CoroutineScope 可以注入

### FullscreenClockActivityTest
1. `activityInjectedFieldsAreNotNull()` - 验证所有 @Inject 字段已填充
2. `viewModelIsCreatedByHilt()` - 验证 ViewModel 通过 Hilt 获取
3. `factoriesAreInjected()` - 验证 Factory 字段已注入

### ViewModelInjectionTest
1. `fullscreenClockViewModelCanBeCreated()` - 验证 ViewModel 创建
2. `savedStateHandleIsInjected()` - 验证 SavedStateHandle 自动提供
3. `applicationContextIsProvided()` - 验证 @ApplicationContext 工作

## 验证步骤
1. 运行 `./gradlew testDebugUnitTest` - 所有测试通过
2. 运行 `./gradlew build` - 编译成功
3. 检查 Hilt 生成的组件代码存在

## 预期结果
- 新增 10-15 个集成测试
- 总测试数达到 46-51 个
- 测试覆盖 Hilt DI 绑定的所有关键路径
- 现有单元测试继续工作（不破坏）
