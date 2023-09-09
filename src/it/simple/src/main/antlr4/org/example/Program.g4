grammar Program;

program: alpha | beta;

alpha: BOOL | NAME;

beta: 'test';

BOOL: 'TRUE' | 'FALSE';

NAME: [a-z] ~[ \r\n\t,.|':;!?\][}{)(]*;
