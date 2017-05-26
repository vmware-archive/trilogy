repo() {
  echo -n $(cd "$(dirname ${BASH_SOURCE[0]})/../../"; pwd)
}

trilogy() {
  echo -n "$(repo)/build/libs/trilogy.jar"
}

projects() {
  echo -n "$(repo)/src/test/resources/projects"
}

test_cases() {
  echo -n "$(repo)/src/test/resources/testcases"
}

run_project() {
  local project="$1"
  java -jar "$(trilogy)" --project="$(projects)/${project}" > /dev/null
}

run_test_case() {
  local test_case="$1"
  java -jar "$(trilogy)" "$(test_cases)/${test_case}.stt" > /dev/null
}

T_OneTestFails() {
  if run_project errors; then
    $T_fail "Expected a non-zero code when at least one test has failed"
  fi
}

T_OneAssertionFails() {
  if run_test_case should_fail_with_sql; then
    $T_fail "Expected a non-zero code when at least one test has failed"
  fi
}