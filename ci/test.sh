#!/bin/bash -ex

boot_oracle() {
  cp ./ci/oracle_setup.sql /docker-entrypoint-initdb.d/
  (./ci/boot_oracle.sh &> /tmp/boot_oracle.log) &
  echo "Waiting for Oracle to finish booting"
  O_SLEEP=0
  while [ ! -f /tmp/oracle.is.ready ]; do
    O_SLEEP=$(expr $(expr ${O_SLEEP} + 20) % 61)
    sleep ${O_SLEEP}
  done
}

prepare_java_runtime() {
  mkdir -p ./lib
  cp /u01/app/oracle-product/12.1.0/xe/jdbc/lib/ojdbc7.jar ./lib
  perl -pi -e 's#(\s+)(testCompile\("org.springframework.boot:spring-boot-starter-test"\))#$1$2\n$1runtime(files("lib/ojdbc7.jar"))#' build.gradle.kts
}

export DB_URL=jdbc:oracle:thin:@$(hostname -i):1521:xe
export DB_USER=app_user
export DB_PASSWORD=secret

pushd ./trilogy
  boot_oracle
  prepare_java_runtime
  ./gradlew clean test
  ./gradlew clean oracleTest
  ./gradlew clean bootRepackage
  TERM=dumb bats ./ci/test/trilogy.bats
popd
