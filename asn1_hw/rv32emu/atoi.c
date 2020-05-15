int atoi(const char* c) {
    int temp = 0, sign = 0;
    if(*c == '+') ++c;
    else if(*c == '-') {
        sign = 1;
        ++c;
    }
    while(*c) {
        if(*c < '0' || *c > '9') return -1;
        temp *= 10;
        temp += *c - '0';
        ++c;
    }
    if(sign) temp = -temp;
    return temp;
}

void print(int n) {
  volatile char* tx = (volatile char*) 0x40002000;
  if(n < 0) {
      *tx = '-';
      n = -n;
  }
  int digit[5] = {n};
  for(; digit[0] >= 10; digit[0] -= 10, ++digit[1]);
  for(; digit[1] >= 10; digit[1] -= 10, ++digit[2]);
  for(; digit[2] >= 10; digit[2] -= 10, ++digit[3]);
  for(; digit[3] >= 10; digit[3] -= 10, ++digit[4]);
  for(int i = 4; i >= 0; --i) *tx = '0' + digit[i];
  return;
}

void _start() {
    const char* c = "-1250";
    print(atoi(c));
    return;
}
