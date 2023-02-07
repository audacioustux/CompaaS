void reverse(char* string, int size) {
    int i = 0;
    int j = size - 1;
    while(i < j) {
        char temp = string[i];
        string[i] = string[j];
        string[j] = temp; 
        i++;
        j--;
    }
}