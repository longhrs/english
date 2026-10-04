# 深圳小学英语（知识引导 + 练习验证）

面向深圳小学一至六年级学生的英语自学 Android 应用。两条主线：

- **知识引导**：按话题单元讲解词汇（含音标、词性、例句）、核心句型和语法点，配自然拼读要点和朗读。
- **练习验证**：根据单元内容自动出题、即时判分并给出解析，错题自动进入错题本和间隔复习队列。

应用**完全离线**运行，不申请任何权限，不联网，学习进度只保存在本机。

## 内容规模

| 项目 | 数量 |
| --- | --- |
| 年级 | 6（一至六年级） |
| 话题单元 | 42（每年级 7 个） |
| 词汇 | 476 条（含音标、词性、中文释义、中英例句） |
| 核心句型 | 126 条（每条带 2 个例句） |
| 语法/用法讲解 | 84 个 |
| 中英例句 | 392 条 |

话题覆盖：问候与自我介绍、颜色、数字、家庭、身体、同学、教室、五感、动物、食物饮料、玩具、
天气季节、一天作息、街道交通、学校、时间、职业、服装、果蔬、家附近的场所、味道与声音、爱好、节日、
深圳这座城市、健康、购物、动物与自然、小花园、校园生活、旅行、饮食烹饪、体育、科技、环保、
风水火与自然力量、理想、中华文化、世界各地、情感与友谊、安全、成长与变化、小升初。

## 与教材的对应关系

深圳小学英语普遍使用**沪教牛津版（深圳用）**（牛津上海版·一年级起点，每册 4 个 Module、12 个 Unit）。
本应用按同样的话题脉络组织内容，并在每个单元的「学习小贴士」里给出一条**教材对照**，写明它对应课本
哪一册、哪个 Module / Unit，方便和学校进度对着用。例如：

| 应用单元 | 教材对照 |
| --- | --- |
| 一年级 · My classmates | 1A Module 1 Getting to know you · Unit 2 My classmates |
| 二年级 · In the street | 2A Module 4 The world around us · Unit 12 In the street |
| 三年级 · Tastes and sounds | 3B Module 1 Using my five senses · Unit 2 Tastes、Unit 3 Sounds |
| 四年级 · My garden | 4B Module 4 Things we enjoy · Unit 10 My garden |
| 五年级 · Wind, water and fire | 5A Module 4 The natural world · Unit 10 Wind、Unit 11 Water、Unit 12 Fire |
| 六年级 · Growing up and changes | 6A Module 1 Unit 1 Growing up、6B Module 1 Unit 2 Changes in our lives |

> 内容说明：词汇、句型和语法**按教材的话题和单元顺序对照编写整理**，措辞为本项目自行撰写，
> **不是任何出版社教材的原文摘录**，也不隶属于任何学校或机构；教材版次会调整，
> 具体请以学校当年所用课本和老师的要求为准。

## 功能

- 首页：连续学习天数、已学词数、总正确率、各年级进度入口
- 知识引导：词卡（英文 / 音标 / 词性 → 中文 + 例句）、标记已掌握、朗读（系统 TTS）、单元词表、句型、语法、学习贴士
- 练习验证：5 种题型自动组卷
  - 中译英选择、英译中选择
  - 单词拼写（三年级及以上才出，低年级以听说认读为主）
  - 句型填空
  - 连词成句
- 即时判分 + 解析 + 朗读，练习报告含得分、星级、用时和错题回顾
- 错题本：答错自动收录，连续答对两次自动移出
- 间隔复习：Leitner 五盒制，间隔 1 / 2 / 4 / 7 天推送复习
- 学习统计：各年级、各单元进度与最好成绩
- 关于页：使用建议、内容说明、隐私说明、一键清空进度

## 怎么拿到 APK

### 方式一：Release 直链（推荐，手机上直接装）

打开 [Releases](https://github.com/longhrs/english/releases) 页面，下载最新版本的
`ShenzhenPrimaryEnglish-vX.Y.apk` 附件即可。仓库是公开的，Release 附件**免登录**，
手机浏览器点开就能下载安装（需允许「安装未知来源应用」）。

Release 只来自稳定分支 `main`，发版流程见下文「分支与发版」。

### 方式二：GitHub Actions 产物（需要登录 GitHub）

每次 push（任何分支）都会自动跑 `.github/workflows/build-apk.yml`：校验内容 → 跑单元测试 → 打包 Debug APK。
开发分支 `dev` 上的试用版就从这里下载。

1. 打开仓库的 **Actions** 标签页
2. 点对应分支最新一次 **Build APK** 运行记录（`dev` 的试用版就选分支为 `dev` 的那条）
3. 在 **Artifacts** 里下载 `ShenzhenPrimaryEnglish-debug-apk`
4. 解压得到 `ShenzhenPrimaryEnglish-debug.apk`，传到安卓手机安装（需允许「安装未知来源应用」）

### 方式三：本地命令行

需要 JDK 17 和 Android SDK（`ANDROID_HOME` 指向 SDK 目录）：

```bash
./gradlew :app:assembleDebug
# 产物：app/build/outputs/apk/debug/app-debug.apk
```

### 方式四：Android Studio

直接用 Android Studio 打开本目录，等 Gradle 同步完成后点 **Run**。

## 分支与发版

| 分支 | 用途 | 推送后 CI 做什么 |
| --- | --- | --- |
| `dev` | 开发分支，日常改动都推这里 | 校验内容 + 单元测试 + 打包 APK（Actions 产物），**不发版** |
| `main` | 稳定分支，只放验证过的版本 | 同上，并按 `versionName` 发布/更新 Release |

一次改动从开发到发版：

1. **在 `dev` 上开发**，推送后等 CI 全绿。
2. **真机验证**：从 Actions 里下载 `dev` 的 APK 装到手机上，把改动涉及的页面点一遍
   （界面、朗读、页面跳转这些 CI 测不到）。
3. **改版本号**：决定发版时，在 `dev` 上把 `app/build.gradle` 的 `versionName` 加一档、
   `versionCode` 加 1，作为这次发版的最后一个提交。
4. **晋级到 `main`**：把 `dev` 快进合并进 `main` 再推送，CI 构建成功后自动发布新 Release。

```bash
git checkout dev && git pull
# ……开发、提交、推送，等 CI 全绿、真机验证通过……
git checkout main && git pull
git merge --ff-only dev      # 只允许快进，保证 main 就是验证过的那个提交
git push origin main         # 触发发版
git checkout dev
```

注意：

- `main` **每推一次都会发版**。同一个 `versionName` 再推一次 `main`，会用新构建的 APK 覆盖该版本
  Release 里的附件，所以只改文档时可以先攒在 `dev`，跟下一次发版一起进 `main`。
- `merge --ff-only` 失败说明 `main` 上有 `dev` 没有的提交，先把 `main` 合回 `dev` 并重新验证，再晋级。
- 也可以打 `v` 开头的标签推上去，或在 Actions 页面手动触发 workflow 并填写版本号来发版。

## 运行要求

- Android 7.0（API 24）及以上
- 朗读功能依赖系统自带的英语 TTS 引擎；没有也不影响其它功能，应用会给出提示

## 工程结构

```
app/src/main/java/com/szprimary/english/
├── core/                    纯 Java 核心层（不依赖 Android，可在 JVM 上直接测试）
│   ├── model/               Word / SentencePattern / GrammarNote / Unit / Grade / Curriculum
│   ├── quiz/                出题（QuizGenerator）、判分（Grader）、答题流程（QuizSession）
│   ├── progress/            学习进度、错题本、Leitner 间隔复习
│   ├── ContentSource.java   内容读取接口（Android 读 assets，测试读文件）
│   └── CurriculumLoader.java
└── ui/                      Android 界面层（全部用代码构建，无布局 XML）
    ├── HomeActivity         首页
    ├── UnitsActivity        单元列表
    ├── LearnActivity        知识引导
    ├── QuizActivity         练习验证
    ├── ResultActivity       练习报告
    ├── ReviewActivity       复习中心 / 错题本
    ├── StatsActivity        学习统计
    └── AboutActivity        关于与设置

app/src/main/assets/curriculum/   grade1.json ... grade6.json（全部课程内容）
app/src/test/java/                单元测试（内容校验、出题、判分、进度）
tools/validate_content.py         课程内容校验脚本（CI 会跑）
```

## 测试

```bash
python3 tools/validate_content.py      # 课程数据结构与质量校验
./gradlew :app:testDebugUnitTest       # 单元测试
```

单元测试覆盖：

- 课程内容完整性：6 个年级 / 42 个单元 / ≥470 词，每个词条的音标、词性、释义、例句都不缺，词条键不重复
- 出题器：对**全部 42 个单元**生成题目并逐题校验——选项 4 个且不重复、正确答案在选项中、
  连词成句的词块能还原成标准句、同一随机种子出题结果可复现、低年级不出拼写题
- 判分：大小写、多余空格、句末标点、中英文引号都不影响判定
- 进度：错题本进出规则、Leitner 盒子升降、复习到期时间、最好成绩、存档读写、存档损坏时的兜底、连续学习天数

## 想改内容

课程内容都在 `app/src/main/assets/curriculum/grade*.json`，改完跑一遍
`python3 tools/validate_content.py` 即可，不需要动代码。单元的 JSON 结构：

```jsonc
{
  "id": "g3u1", "no": 1,
  "title_en": "My school", "title_cn": "我的学校", "topic": "School",
  "overview": "本单元学什么……",
  "phonics": { "focus": "语音重点", "tip": "怎么发音", "examples": ["school /skuːl/ 学校"] },
  "words":    [ { "en": "school", "ipa": "/skuːl/", "pos": "n.", "cn": "学校",
                  "ex_en": "Our school is in Futian.", "ex_cn": "我们学校在福田。" } ],
  "patterns": [ { "en": "There is a ... in our school.", "cn": "我们学校里有一个……。",
                  "examples": [ { "en": "...", "cn": "……" } ] } ],
  "grammar":  [ { "title": "There is 和 There are", "explain": "……",
                  "examples": [ { "en": "...", "cn": "……" } ] } ],
  "tips": ["学习小贴士"]
}
```
