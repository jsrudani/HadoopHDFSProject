run() {
                        echo "\$ ${@}"
                        "${@}"
                        res=$?
                        if [ $res != 0 ]; then
                          echo
                          echo "Failed!"
                          echo
                          exit $res
                        fi
                      }

                      run tar cf hadoop-2.3.0.tar hadoop-2.3.0
                      run gzip -f hadoop-2.3.0.tar
                      echo
                      echo "Hadoop dist tar available at: /home/jsrudani/hadoopcache/hadoopsrccode/hadoop-dist/target/hadoop-2.3.0.tar.gz"
                      echo