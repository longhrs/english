#!/usr/bin/env python3
"""校验 assets/curriculum/*.json 的结构和内容质量。

CI 和本地都用这个脚本；有 ERROR 时退出码非 0。
"""
import glob
import json
import os
import re
import sys

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "app", "src", "main", "assets", "curriculum")
WORD_FIELDS = ["en", "ipa", "pos", "cn", "ex_en", "ex_cn"]

errors = []
warnings = []
seen_words = {}
seen_unit_ids = set()
stats = {"grades": 0, "units": 0, "words": 0, "patterns": 0, "grammar": 0, "examples": 0}


def err(where, msg):
    errors.append("%s: %s" % (where, msg))


def warn(where, msg):
    warnings.append("%s: %s" % (where, msg))


def check_example(where, ex):
    stats["examples"] += 1
    for key in ("en", "cn"):
        if not ex.get(key, "").strip():
            err(where, "例句缺少 %s" % key)
    if len(ex.get("en", "").split()) < 2:
        warn(where, "英文例句过短: %r" % ex.get("en"))


for path in sorted(glob.glob(os.path.join(ROOT, "grade*.json"))):
    name = os.path.basename(path)
    try:
        data = json.load(open(path, encoding="utf-8"))
    except ValueError as e:
        err(name, "JSON 解析失败: %s" % e)
        continue

    stats["grades"] += 1
    grade = data.get("grade")
    if not isinstance(grade, int) or not 1 <= grade <= 6:
        err(name, "grade 字段非法: %r" % grade)
    if not data.get("title"):
        err(name, "缺少 title")

    units = data.get("units", [])
    if len(units) < 4:
        warn(name, "只有 %d 个单元" % len(units))

    for unit in units:
        stats["units"] += 1
        uid = unit.get("id", "?")
        where = "%s/%s" % (name, uid)
        if uid in seen_unit_ids:
            err(where, "单元 id 重复")
        seen_unit_ids.add(uid)
        expected = "g%du%s" % (grade, unit.get("no"))
        if uid != expected:
            err(where, "单元 id 应为 %s" % expected)
        for key in ("title_en", "title_cn", "overview"):
            if not unit.get(key, "").strip():
                err(where, "缺少 %s" % key)

        words = unit.get("words", [])
        if len(words) < 8:
            warn(where, "词汇只有 %d 个" % len(words))
        unit_words = set()
        for word in words:
            stats["words"] += 1
            en = word.get("en", "")
            spot = "%s/%s" % (where, en or "?")
            for field in WORD_FIELDS:
                if not word.get(field, "").strip():
                    err(spot, "缺少字段 %s" % field)
            ipa = word.get("ipa", "")
            if not (ipa.startswith("/") and ipa.endswith("/") and len(ipa) > 2):
                err(spot, "音标格式应为 /.../，当前为 %r" % ipa)
            if not re.match(r"^[A-Za-z][A-Za-z '\-]*$", en):
                err(spot, "单词含非法字符: %r" % en)
            key = en.lower()
            if key in unit_words:
                err(spot, "同一单元内单词重复")
            unit_words.add(key)
            seen_words.setdefault(key, []).append(where)
            # 例句最好包含该单词（允许 -s/-es/-ies/-ing 等变形），便于在语境中记忆
            stem = re.split(r"[ '\-]", en.lower())[0]
            if len(stem) > 4 and stem[-1] in "ye":
                stem = stem[:-1]
            if stem and stem not in word.get("ex_en", "").lower():
                warn(spot, "例句未包含该单词: %r" % word.get("ex_en"))

        patterns = unit.get("patterns", [])
        if len(patterns) < 2:
            warn(where, "句型只有 %d 条" % len(patterns))
        for pattern in patterns:
            stats["patterns"] += 1
            spot = "%s/pattern:%s" % (where, pattern.get("en", "?"))
            for key in ("en", "cn"):
                if not pattern.get(key, "").strip():
                    err(spot, "缺少 %s" % key)
            examples = pattern.get("examples", [])
            if not examples:
                err(spot, "句型没有例句，无法出连词成句题")
            for ex in examples:
                check_example(spot, ex)

        for note in unit.get("grammar", []):
            stats["grammar"] += 1
            spot = "%s/grammar:%s" % (where, note.get("title", "?"))
            for key in ("title", "explain"):
                if not note.get(key, "").strip():
                    err(spot, "缺少 %s" % key)
            for ex in note.get("examples", []):
                check_example(spot, ex)

        phonics = unit.get("phonics")
        if phonics and not phonics.get("focus", "").strip():
            err(where, "phonics 缺少 focus")

# 选择题至少需要 4 个选项，词库太小会出不了题
for path in sorted(glob.glob(os.path.join(ROOT, "grade*.json"))):
    data = json.load(open(path, encoding="utf-8"))
    for unit in data.get("units", []):
        if len(unit.get("words", [])) < 4:
            err(unit.get("id", "?"), "词汇不足 4 个，无法生成选择题")

repeats = {w: v for w, v in seen_words.items() if len(v) > 1}

print("统计：%d 个年级 / %d 个单元 / %d 个词条 / %d 条句型 / %d 个语法点 / %d 条例句"
      % (stats["grades"], stats["units"], stats["words"], stats["patterns"], stats["grammar"], stats["examples"]))
if repeats:
    print("跨单元复现的词（螺旋式复现，属正常）：%d 个 -> %s"
          % (len(repeats), ", ".join(sorted(repeats)[:12])))
for w in warnings:
    print("WARN  " + w)
for e in errors:
    print("ERROR " + e)
print("warnings=%d errors=%d" % (len(warnings), len(errors)))
sys.exit(1 if errors else 0)
