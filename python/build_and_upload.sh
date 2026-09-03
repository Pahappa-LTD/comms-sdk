#!/bin/bash
set -euo pipefail

rm -rf dist || true
python -m pip install build twine
python -m build
python -m twine upload dist/* -p "${PYPI_TOKEN}"
