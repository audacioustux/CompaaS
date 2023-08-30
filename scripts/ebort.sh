#!/usr/bin/env bash

__usage() {
    cat <<EOF

Exponential BackOff and Re-Try (EBORT)

Re-try a command with exponential backoff. The command is re-tried until it
either succeeds, or the maximum number of attempts is reached, or the maximum
wait time is reached.

Usage: ebor.sh [-h] [-d DELAY] [-m MAX_ATTEMPTS] [-w MAX_WAIT] [-u MAX_DELAY] [-v] [--] COMMAND [ARG]...
Options:
  -h, --help            show this help message and exit
  -d DELAY, --delay DELAY
                        initial delay in seconds, default 1
  -m MAX_ATTEMPTS, --max-attempts MAX_ATTEMPTS
                        maximum number of attempts, default -1 (infinite)
  -w MAX_WAIT, --max-wait MAX_WAIT
                        maximum wait in seconds, default -1 (infinite)
  -u MAX_DELAY, --max-delay MAX_DELAY
                        maximum delay in seconds, default 60
  -v, --verbose         verbose output, default false
                        prints the attempt number before each attempt
                        and the delay before each attempt
  --                    end of options
Arguments:
  COMMAND               command to run
  ARG                   arguments to pass to command

Examples:
  ebort.sh echo hello
  ebort.sh -d 5 -m 3 -u 10 echo hello
  ebort.sh -d 5 -m 3 -u 10 -v -- ls /tmp/does-not-exist
  ebort.sh -d 5 -m 3 -u 10 -w 60 -- ls /tmp/does-not-exist
EOF
}

# parse command line arguments
__parse_args() {
    # default values
    DELAY=1
    MAX_ATTEMPTS=-1
    MAX_WAIT=-1
    MAX_DELAY=60
    VERBOSE=false

    # parse options
    while [[ $# -gt 0 ]]; do
        case "$1" in
            -h|--help)
                __usage
                exit 0
                ;;
            -d|--delay)
                DELAY="$2"
                shift 2
                ;;
            -m|--max-attempts)
                MAX_ATTEMPTS="$2"
                shift 2
                ;;
            -w|--max-wait)
                MAX_WAIT="$2"
                shift 2
                ;;
            -u|--max-delay)
                MAX_DELAY="$2"
                shift 2
                ;;
            -v|--verbose)
                VERBOSE=true
                shift
                ;;
            --)
                shift
                break
                ;;
            -*)
                echo "Unknown option: $1" >&2
                exit 1
                ;;
            *)
                break
                ;;
        esac
    done

    # parse arguments
    if [[ $# -eq 0 ]]; then
        echo "Missing command" >&2
        exit 1
    fi
    COMMAND="$1"
    shift
    ARGS=("$@")
}

# exponential backoff and re-try
__ebort() {
    local attempt=0
    local delay="$DELAY"
    local max_attempts="$MAX_ATTEMPTS"
    local max_wait="$MAX_WAIT"
    local max_delay="$MAX_DELAY"
    local verbose="$VERBOSE"
    local command="$COMMAND"
    local args=("${ARGS[@]}")

    # re-try loop
    while true; do
        # run command
        if $verbose; then
            echo "Attempt $((attempt + 1)):"
        fi
        "$command" "${args[@]}"
        local exit_code="$?"

        # exit if command succeeded
        if [[ "$exit_code" -eq 0 ]]; then
            return 0
        fi

        # exit if maximum number of attempts reached
        if [[ "$max_attempts" -gt 0 ]] && [[ "$attempt" -ge "$max_attempts" ]]; then
            return "$exit_code"
        fi

        # exit if maximum wait time reached
        if [[ "$max_wait" -gt 0 ]] && [[ "$delay" -gt "$max_wait" ]]; then
            return "$exit_code"
        fi

        # increase delay
        delay=$((delay * 2))
        if [[ "$delay" -gt "$max_delay" ]]; then
            delay="$max_delay"
        fi

        # wait
        if $verbose; then
            echo "Waiting $delay seconds..."
        fi
        sleep "$delay"

        # increase attempt
        attempt=$((attempt + 1))
    done
}

# main
__parse_args "$@"
__ebort
