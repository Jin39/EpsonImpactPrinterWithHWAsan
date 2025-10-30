#!/system/bin/sh

# HWASan wrapper script
# This script sets up the environment for Hardware Address Sanitizer

HERE="$(cd "$(dirname "$0")" && pwd)"

# Set HWASan options
export HWASAN_OPTIONS="halt_on_error=1:abort_on_error=1:print_stats=1:print_module_map=1"

# Set LD_PRELOAD for HWASan runtime
# The HWASan runtime library will be automatically provided by the NDK
export LD_PRELOAD=""

# Optional: Set symbolizer path for better stack traces
# export HWASAN_SYMBOLIZER_PATH="/system/bin/llvm-symbolizer"

# Execute the original command
exec "$@"