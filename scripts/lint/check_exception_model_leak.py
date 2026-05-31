#!/usr/bin/env python3
"""
catch (Exception e) 또는 catch (RuntimeException e) 같은 광범위한 예외를 잡으면서
model.addAttribute(..., e.getMessage()) 를 호출하는 블록을 감지한다.
IllegalArgumentException, IllegalStateException 등 구체적인 비즈니스 예외는 허용.
"""
import sys
import re

DANGEROUS_CATCH = re.compile(
    r'catch\s*\(\s*(Exception|RuntimeException|Throwable|Error)\s+\w+\s*\)'
)
MODEL_GET_MESSAGE = re.compile(r'model\.addAttribute\s*\([^)]+\.getMessage\(\)')

violations = []

for path in sys.argv[1:]:
    try:
        with open(path, encoding='utf-8') as f:
            lines = f.readlines()
    except Exception:
        continue

    in_dangerous_catch = False
    brace_depth = 0
    catch_line = -1

    for i, line in enumerate(lines, 1):
        if DANGEROUS_CATCH.search(line):
            in_dangerous_catch = True
            brace_depth = 0
            catch_line = i

        if in_dangerous_catch:
            opens = line.count('{')
            closes = line.count('}')
            brace_depth += opens - closes

            if MODEL_GET_MESSAGE.search(line):
                violations.append(f"{path}:{i}: {line.rstrip()}")

            # catch_line 자신이 '{' 를 포함하지 않을 수도 있으므로 depth <= 0 조건에 catch_line 제외
            if brace_depth <= 0 and i > catch_line:
                in_dangerous_catch = False
                brace_depth = 0

if violations:
    for v in violations:
        print(v)
    sys.exit(1)
sys.exit(0)
