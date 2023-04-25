import random
import numpy as np
from tabulate import tabulate

def gen_request(maximum, process):
    return np.array(
        [random.randint(0, maximum[process][i]) for i in range(len(maximum))]
    )
    
def get_valid_index(finish, need, work, n):
    index = None
    for i in range(n):
        if not finish[i] and (need[i] <= work).all():
            index = i
            break
    return index
    
def is_safe(available, need, allocation, n):
    finish = np.array([False]*n)
    work = available.copy()
    index = get_valid_index(finish, need, work, n)
    while (index is not None):
        work += allocation[index]
        finish[index] = True
        index = get_valid_index(finish, need, work, n)
    if finish.all():
        return True
    else:
        return False
    
def print_state(available, allocation, need, message):
    print(message, '\n',
        tabulate(tabular_data=[[allocation, need, available]],
                headers=['Allocation', 'Need', 'Available']), '\n')

def execute_algorithm(available, maximum, allocation, need, n, m):
    
    process = random.randint(0, n-1)
    request = gen_request(maximum, process)
    print('Making request from process', process, ':', request)
    
    print_state(available, allocation, need, '\n----Previous State----')
    
    if (request <= need[process]).all():
        if (request <= available).all():
            available -= request
            allocation[process] += request
            need[process] -= request
        else:
            print('Resources are not available for the request',
                  '\n', request, '>', available, '\n')
            exit(0)
    else:
        print('Error while doing the request, process exceeds the maximum claim',
              '\n', request, '>', need[process])
        exit(0)
    
    print_state(available, allocation, need, '\n----Current State----')
    
    if is_safe(available, need, allocation, n):
        print('The system is safe on the current state')
    else:
        print('Warning: the system is not safe on the current state')

if __name__ == '__main__':
    n, m = 3, 3
    available = np.array([4, 2, 6])
    maximum = np.array([
        [4, 3, 5], 
        [5, 5, 5], 
        [3, 4, 3]])
    allocation = np.array([
        [2, 1, 3], 
        [3, 2, 2], 
        [1, 3, 1]])
    need = maximum - allocation
    execute_algorithm(available, maximum, allocation, need, n, m)
    