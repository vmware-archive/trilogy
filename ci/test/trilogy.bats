repo() {
  echo -n $(cd "${BATS_TEST_DIRNAME}/../../"; pwd)
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
  java -jar "$(trilogy)" --project="$(projects)/${project}"
}

run_test_case() {
  local test_case="$1"
  java -jar "$(trilogy)" "$(test_cases)/${test_case}.stt"
}

@test "successful run" {
  run_project mixed_complete
}

@test "a test fails" {
  run run_project errors
  [[ "${status}" -eq 1 ]]
  [[ "${output}" =~ "[FAIL] Testable errors - Wrong error specified" ]]
}

@test "an assertion fails" {
  run run_test_case should_fail_with_sql
  [[ "${status}" -eq 1 ]]
  [[ "${output}" =~ "Assertion failure: Assertion description" ]]
}

@test "a parsing error" {
  run run_project broken_test_cases
  [[ "${status}" -eq 1 ]]
  [[ "${output}" =~ "the first line of a test case should start with \`# TEST CASE\`" ]]
}

@test "broken schema" {
  run run_project broken_schema
  [[ "${status}" -eq 1 ]]
  [[ "${output}" =~ "[FAIL] Unable to load schema:" ]]
}

@test "broken SQL source" {
  run run_project broken_source
  [[ "${status}" -eq 1 ]]
  [[ "${output}" =~ "[FAIL] Unable to load script " ]]
}

@test "non-existing procedure" {
  run run_test_case non_existing_procedure_name
  [[ "${status}" -eq 1 ]]
  [[ "${output}" =~ "Unable to determine the correct call signature - no procedure/function/signature for 'AHOY'" ]]
}

@test "database connection failure" {
  DB_USER=r4nd0m_stuff run run_project mixed_complete
  [[ "${status}" -eq 1 ]]
  [[ "${output}" =~ "Unable to connect to the database:" ]]
}