"""TDD 红色阶段：repo_sync（浅克隆同步器）的行为规格。

repo_sync 负责把 AOSP/LineageOS manifest 浅克隆到本地，供构建 job 使用：
- repo init：必须带 --depth=1（浅克隆），manifest URL 与分支可配置；
- repo sync：默认只同步当前分支（-c），并发数可配，可按项目列表局部同步；
- --dry-run 模式不执行命令，只打印将要执行的命令序列（便于 CI 里调试）。
"""
import os
import sys
import unittest

sys.path.insert(0, os.path.join(os.path.dirname(__file__), "..", "scripts"))

from repo_sync import build_init_cmd, build_sync_cmd, plan_commands  # noqa: E402


class RepoSyncTest(unittest.TestCase):
    MANIFEST = "https://android.googlesource.com/platform/manifest"
    BRANCH = "android-latest-release"

    def test_init_cmd_contains_shallow_and_branch(self):
        cmd = build_init_cmd(self.MANIFEST, self.BRANCH)
        self.assertIn("--depth=1", cmd)
        self.assertIn("-b", cmd)
        self.assertTrue(any(a == self.BRANCH for a in cmd))
        self.assertIn("-u", cmd)
        self.assertTrue(any(a == self.MANIFEST for a in cmd))

    def test_sync_cmd_current_only_and_jobs(self):
        cmd = build_sync_cmd(jobs=4)
        self.assertIn("-c", cmd)
        self.assertIn("-j4", cmd)

    def test_sync_can_target_specific_projects(self):
        cmd = build_sync_cmd(projects=["build", "frameworks/base"])
        self.assertIn("build", cmd)
        self.assertIn("frameworks/base", cmd)

    def test_plan_commands_is_list_and_starts_with_init(self):
        plan = plan_commands(self.MANIFEST, self.BRANCH, jobs=2)
        self.assertIsInstance(plan, list)
        self.assertGreaterEqual(len(plan), 2)
        self.assertEqual(plan[0][0], "repo")
        self.assertIn("init", plan[0])


if __name__ == "__main__":
    unittest.main()