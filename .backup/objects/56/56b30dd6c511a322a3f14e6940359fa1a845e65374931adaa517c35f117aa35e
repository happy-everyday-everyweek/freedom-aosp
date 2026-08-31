#!/usr/bin/env python3
"""构建产物校验器（TDD 结果：红→绿）。

校验产物目录中的镜像文件，供 CI 构建结束后执行：
- system.img 必须存在、非空、不超过上限（默认 2GB）；
- boot.img / vendor.img 可选，存在时同样校验；
- 全部通过退出码 0，任一失败退出码 1，并逐文件报告状态。
"""
import argparse
import os
import sys
from dataclasses import dataclass


@dataclass
class ArtifactResult:
    path: str
    ok: bool
    size: int = 0
    reason: str = ""


# (文件名, 大小上限, 是否必需)
ARTIFACT_RULES = [
    ("system.img", 2 << 30, True),
    ("boot.img", 64 << 20, False),
    ("vendor.img", 1 << 30, False),
]


def check_artifacts(art_dir: str, system_max: int = 2 << 30) -> list[ArtifactResult]:
    results: list[ArtifactResult] = []
    for name, max_size, required in ARTIFACT_RULES:
        if name == "system.img":
            max_size = system_max
        path = os.path.join(art_dir, name)
        if not os.path.exists(path):
            if required:
                results.append(ArtifactResult(path=name, ok=False, reason="missing"))
            continue
        size = os.path.getsize(path)
        if size == 0:
            results.append(ArtifactResult(path=name, ok=False, size=size, reason="empty"))
        elif size > max_size:
            results.append(ArtifactResult(path=name, ok=False, size=size, reason="exceeds max size"))
        else:
            results.append(ArtifactResult(path=name, ok=True, size=size))
    return results


def main() -> int:
    parser = argparse.ArgumentParser(description="校验 AOSP 构建产物镜像")
    parser.add_argument("--dir", required=True, help="产物目录（含 system.img 等）")
    parser.add_argument("--system-max", type=int, default=2 << 30, help="system.img 大小上限（字节）")
    args = parser.parse_args()

    results = check_artifacts(args.dir, args.system_max)
    for r in results:
        print(f"{r.path}: {'OK' if r.ok else 'FAIL'} ({r.size} bytes" + (f", {r.reason}" if r.reason else "") + ")")
    return 0 if all(r.ok for r in results) else 1


if __name__ == "__main__":
    sys.exit(main())
