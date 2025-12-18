@echo off
pip install build twine && python -m build && python -m twine upload dist/* -p "publishing-api-token"