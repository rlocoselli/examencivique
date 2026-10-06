import sys
import unittest
from pathlib import Path
from unittest.mock import MagicMock

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from next_play_version import FIRST_VERSION_CODE, MAX_VERSION_CODE, next_version, read_next_version


class PlayVersionTests(unittest.TestCase):
    def test_existing_numbering_is_preserved(self):
        self.assertEqual(next_version({}, {}, {}), (FIRST_VERSION_CODE + 1, "1.0.1"))

    def test_highest_code_across_bundles_apks_and_tracks_wins(self):
        code, name = next_version(
            {"bundles": [{"versionCode": FIRST_VERSION_CODE + 3}]},
            {"apks": [{"versionCode": FIRST_VERSION_CODE + 7}]},
            {"tracks": [{"releases": [{"status": "draft", "versionCodes": [str(FIRST_VERSION_CODE + 9)]}]}]})
        self.assertEqual((code, name), (FIRST_VERSION_CODE + 10, "1.0.10"))

    def test_retry_after_upload_uses_another_code(self):
        first, _ = next_version({}, {}, {})
        second, _ = next_version({"bundles": [{"versionCode": first}]}, {}, {})
        self.assertEqual(second, first + 1)

    def test_android_version_limit_fails_instead_of_reusing_a_code(self):
        with self.assertRaises(ValueError):
            next_version({"bundles": [{"versionCode": MAX_VERSION_CODE}]}, {}, {})

    def test_temporary_edit_is_deleted_even_when_query_fails(self):
        service = MagicMock()
        edits = service.edits.return_value
        edits.insert.return_value.execute.return_value = {"id": "temporary"}
        edits.bundles.return_value.list.return_value.execute.side_effect = RuntimeError("API unavailable")
        with self.assertRaises(RuntimeError):
            read_next_version(service, "test.package")
        edits.delete.assert_called_once_with(packageName="test.package", editId="temporary")
        edits.commit.assert_not_called()


if __name__ == "__main__":
    unittest.main()
