repo() {
  echo -n $(cd "$(dirname ${BASH_SOURCE[0]})/../../"; pwd)
}

trilogy() {
  echo -n "$(repo)/build/libs/trilogy.jar"
}

projects() {
  echo -n "$(repo)/src/test/resources/projects"
}

run_project() {
  local project="$1"
  java -jar "$(trilogy)" --project="$(projects)/${project}" > /dev/null
  echo $?
}
T_OneTestFails() {
  if [[ "$(run_project errors)" == "0" ]]; then
    $T_fail "Expected a non-zero code when at least one test has failed"
  fi
}
