# tools

- `validate_content.py`：校验 `app/src/main/assets/curriculum/*.json` 的结构和内容质量
  （字段完整性、音标格式、单元内词汇去重、选择题可出题性、例句质量）。
  CI 在编译前会先跑它，有 ERROR 时构建失败。本地用：

  ```bash
  python3 tools/validate_content.py
  ```
