"""TDD 红色阶段：产物校验器 artifact_checker 的行为规格。

校验构建产物目录中的镜像文件：
- system.img / boot.img / vendor.img 必须存在、非空；
- 每个文件大小不得超过对应上限（system 默认 2GB，boot/vendor 各自上限）；
- 全部通过时退出码 0，任一失败退出码 1，并报告每个文件的状态。
"""
import os
import sys
import tempfile
import unittest

sys.path.insert(0, os.path.join(os.path.dirname(__file__), "..", "scripts"))

from artifact_checker import check_artifacts, ArtifactResult


def make_fake_image(path, size):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.truncate(size)


class ArtifactCheckerTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.art_dir = self.tmp.name

    def test_missing_system_img_fails(self):
        """缺 system.img 必须判失败并说明缺哪个文件。"""
        results = check_artifacts(self.art_dir)
        self.assertEqual(len(results), 1)
        self.assertFalse(results[0].ok)
        self.assertIn("system.img", results[0].path)

    def test_empty_system_img_fails(self):
        """存在但为空的镜像判失败。"""
        make_fake_image(os.path.join(self.art_dir, "system.img"), 0)
        results = check_artifacts(self.art_dir)
        self.assertFalse(results[0].ok)

    def test_windows_sized_system_img_passes(self):
        """2GB 以内的 system.img 通过，报告 ok 且记录大小。"""
        make_fake_image(os.path.join(self.art_dir, "system.img"), 1 << 20)
        results = check_artifacts(self.art_dir)
        self.assertEqual(len(results), 1)
        self.assertTrue(results[0].ok)
        self.assertEqual(results[0].size, 1 << 20)

    def test_oversized_system_img_fails(self):
        """超过上限的 system.img 判失败并报告超限原因。"""
        make_fake_image(os.path.join(self.art_dir, "system.img"), (2 << 30) + 1)
        results = check_artifacts(self.art_dir)
        self.assertFalse(results[0].ok)

    def test_boot_and_vendor_optional_present(self):
        """boot.img / vendor.img 存在时也要被校验。"""
        make_fake_image(os.path.join(self.art_dir, "system.img"), 1 << 20)
        make_fake_image(os.path.join(self.art_dir, "boot.img"), 1 << 20)
        make_fake_image(os.path.join(self.art_dir, "vendor.img"), 1 << 20)
        results = check_artifacts(self.art_dir)
        self.assertEqual(len(results), 3)
        self.assertTrue(all(r.ok for r in results))


if __name__ == "__main__":
    unittest.main()