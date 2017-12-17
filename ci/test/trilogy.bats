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
  [[ "$status" -eq 1 ]]
  [[ "$output" =~ "[FAIL] Testable errors - Wrong error specified" ]]
}

@test "an assertion fails" {
  run run_test_case should_fail_with_sql
  [[ "$status" -eq 1 ]]
  [[ "$output" =~ "Assertion failure: Assertion description" ]]
}

@test "a parsing error" {
  run run_project broken_test_cases
  [[ "$status" -eq 1 ]]
  [[ "$output" =~ "the first line of a test case should start with \`# TEST CASE\`" ]]
}

@test "broken schema" {
  run run_project broken_schema
  [[ "$status" -eq 1 ]]
  [[ "$output" =~ "[FAIL] Unable to load schema:" ]]
}

@test "broken SQL source" {
  run run_project broken_source
  [[ "$status" -eq 1 ]]
  [[ "$output" =~ "[FAIL] Unable to load script " ]]
}

@test "non-existing procedure" {
  run run_test_case non_existing_procedure_name
  [[ "$status" -eq 1 ]]
  [[ "$output" =~ "the specified procedure, AHOY does not exist" ]]
}

@test "database connection failure" {
  DB_USER=r4nd0m_stuff run run_project mixed_complete
  [[ "$status" -eq 1 ]]
  [[ "$output" =~ "Unable to connect to the database:" ]]
}

@test "missing fixtures" {
  run run_project missing_fixtures
  [[ "$status" -eq 1 ]]
  [[ "$output" =~ "Unable to find fixture 'Setup non-existing client'" ]]
}

@test "broken fixtures" {
  run run_project broken_fixtures
  [[ "$status" -eq 1 ]]
  [[ "$output" =~ "Unable to load the 'Setup client' setup fixture" ]]
}

@test "regular fixtures" {
  run_project setup_teardown
}

@test "failing fixtures" {
  run run_project setup_teardown_with_failure
  [[ "$status" -eq 1 ]]
  [[ "$output" =~ "[FAIL] One more example - The client balance should not be valid anymore" ]]
}

@test "non-existing project" {
  run run_project "s43lk5jlk5j"
  [[ "$status" -eq 1 ]]
  echo "$output"
  [[ "$output" =~ "[FAIL] Nothing to run. Unable to find the test project folder " ]]
}

@test "non-existing test case" {
  run run_test_case foos_behind_bars
  [[ "$status" -eq 1 ]]
  [[ "$output" =~ "[FAIL] Nothing to run. No such file " ]]
}

@test "wrong file for a test case" {
  run java -jar "$(trilogy)" "$(repo)/src/test/resources/tables/completeTable.md"
  [[ "$status" -eq 1 ]]
  echo "$output"
  [[ "$output" =~ "[FAIL] Invalid test case filename" ]]
}

@test "unavailable driver in jdbc URL" {
  DB_URL=jdbc:mysql://host1:33060/sakila run run_project setup_teardown
  [[ "$status" -eq 1 ]]
  [[ "$output" =~ "[FAIL] Unable to load the driver for URL jdbc:mysql://host1:33060/sakila. Please make sure the URL is correct, and the appropriate JDBC driver is installed." ]]
}