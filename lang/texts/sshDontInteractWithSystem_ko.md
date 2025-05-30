## 셸 유형 감지

XPipe는 연결의 셸 유형을 감지한 다음 활성 셸과 상호 작용하는 방식으로 작동합니다. 그러나 이 방식은 셸 유형을 알고 있고 특정 작업 및 명령을 지원하는 경우에만 작동합니다. `bash`, `cmd`, `powershell` 등과 같은 모든 일반적인 셸이 지원됩니다.

## 알 수 없는 셸 유형

라우터, 링크 또는 일부 IOT 장치와 같이 알려진 명령 셸을 실행하지 않는 시스템에 연결할 경우 XPipe가 셸 유형을 감지하지 못하고 잠시 후 오류가 발생합니다. 이 옵션을 사용 설정하면 XPipe가 셸 유형을 식별하지 않고 셸을 있는 그대로 실행하려고 시도하지 않습니다. 이렇게 하면 오류 없이 연결을 열 수 있지만 파일 브라우저, 스크립팅, 하위 연결 등 많은 기능이 이 연결에서 지원되지 않습니다.
