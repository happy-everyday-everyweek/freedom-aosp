#!/usr/bin/env python3
"""AOSP/LineageOS 浅克隆同步器（TDD：红→绿）。

供构建 job 使用：repo init（--depth=1 浅克隆）+ repo sync（当前分支、可局部同步）。
--dry-run 只打印计划命令，不执行。
"""
import argparse
import shlex
import subprocess
import sys


def build_init_cmd(manifest_url: str, branch: str, depth: int = 1) -> list[str]:
    cmd = ["repo", "init", "-u", manifest_url, "-b", branch]
    if depth:
        cmd.append(f"--depth={depth}")
    cmd.append("--no-repo-verify")
    return cmd


def build_sync_cmd(jobs: int = 4, projects: list[str] | None = None) -> list[str]:
    cmd = ["repo", "sync", "-c", f"-j{jobs}", "--no-clone-bundle", "--no-tags"]
    if projects:
        cmd.extend(projects)
    return cmd


def plan_commands(manifest_url: str, branch: str, jobs: int = 4,
                  projects: list[str] | None = None) -> list[list[str]]:
    return [
        build_init_cmd(manifest_url, branch),
        build_sync_cmd(jobs=jobs, projects=projects),
    ]


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="AOSP 浅克隆同步器")
    parser.add_argument("--manifest", default="https://android.googlesource.com/platform/manifest")
    parser.add_argument("--branch", default="android-latest-release")
    parser.add_argument("--jobs", type=int, default=4)
    parser.add_argument("--projects", nargs="*", default=None, help="仅同步指定项目")
    parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args(argv)

    plan = plan_commands(args.manifest, args.branch, args.jobs, args.projects)
    for cmd in plan:
        print("+ " + shlex.join(cmd))
        if not args.dry_run:
            proc = subprocess.run(cmd)
            if proc.returncode != 0:
                print(f"command failed: {shlex.join(cmd)}", file=sys.stderr)
                return proc.returncode
    return 0


if __name__ == "__main__":
    sys.exit(main())