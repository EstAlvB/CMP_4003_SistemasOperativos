"""
Algoritmo del banquero

Realizado por:
	- Gabriel Lara
	- Alexander Pastor
	- Esteban Alvarado
"""

import numpy as np
from tabulate import tabulate
import random
import sys
import os

def tryARequest(process, request, avail, maxm, allot):
    print(f'Generating a request from process {process}:', request)
    need = calculateNeed(maxm, allot)
    if requestCanBeSatisfied(process, request, avail, need):
        allocateResources(process, request, avail, allot, need)
        return True
    return False

# Function to find the need of each process
def calculateNeed(maxm, allot):
	return maxm - allot

def canBeSatisfied(process, avail, need):
	if (need[process] > avail).all():
		return False
	return True

def requestCanBeSatisfied(process, request, avail, need):
    if (request <= need[process]).all():
        if (request <= avail).all():
            return True
        else:
            print('Resources are not available for the request', '\n', request, '>', avail)
            return False
    else:
        print('Error while doing the request, process exceeds the maximum claim', '\n', request, '>', need[process])
        return False

def allocateResources(process, request, avail, allot, need):
	avail -= request
	allot[process] += request
	need[process] -= request

def releaseResources(process, release, avail, allot, need):
	avail += release
	allot[process] -= release
	need[process] += release
  
def getMatrixbyInput(message, R, P=0):
    print('\n' + message)
    matrix = []
    for i in range(P):
        row = []
        for j in range(R):
            while True:
                try:
                    value = input(f'Value for row {i} and column {j}: ')
                    if value == "":
                        raise ValueError("Empty input")
                    row.append(int(value))
                    break
                except ValueError:
                    print("Invalid input. Please enter a non-empty integer value.")
        matrix.append(row)
        # Clear the terminal screen
        os.system('cls' if os.name == 'nt' else 'clear')
        print(message, '\n', matrix)
    if P == 0:
        for i in range(R):
            while True:
                try:
                    value = input(f'Value for {i}: ')
                    if value == "":
                        raise ValueError("Empty input")
                    matrix.append(int(value))
                    break
                except ValueError:
                    print("Invalid input. Please enter a non-empty integer value.")
    return matrix

def printCurrentState(avail, maxm, allot, need):
    print('\n', 
          tabulate([[avail, maxm, allot, need]], 
                   headers=['Available', 'Max', 'Allocation', 'Need']), 
          '\n')

# Function to find the system is in safe state or not
def isSafe(avail, maxm, allot, P):
	
	# Function to calculate need matrix
	need = calculateNeed(maxm, allot)

	# Mark all processes as infinish
	finish = np.array([False] * P)
	
	# To store safe sequence
	safeSeq = []

	# Make a copy of available resources
	work = np.copy(avail)

	# Check if all processes can be finished
	while not finish.all():
		found = False
		# Check if there is a process that can be finished
		for p in range(P):
			if not finish[p] and canBeSatisfied(p, work, need):
				found = True
				# Add the resources of the finished process to the available resources
				work += allot[p]
				finish[p] = True
				safeSeq.append(p)
				break
		# If no process can be finished, the system is not in a safe state
		if not found:
			printCurrentState(avail, maxm, allot, need)
			return False, []

	# If all processes can be finished, the system is in a safe state and safe sequence is safeSeq
	printCurrentState(avail, maxm, allot, need)
	return True, safeSeq


# Driver code
if __name__ =="__main__":
    
    # Number of processes and number of resources types set by terminal
    P, R = int(sys.argv[1]), int(sys.argv[2])
    
    #Create process list
    processes = list(range(0, P))
    
    # Set the maximum number of resources that can be allocated to each process
    maxm = getMatrixbyInput('Matrix for maximum number of instances of each type of resource for each process', R, P)
    maxm = np.array(maxm)
    
    # Set the amount of resources currently allocated to each process
    allot = getMatrixbyInput('Matrix for number of resources allocated in each process', R, P)
    allot = np.array(allot)
    
    # Set the amount of available resources
    avail = getMatrixbyInput('Array for the amount of available resources', R)
    avail = np.array(avail)
    
    # Check if the system is in a safe state
    is_safe, safe_seq = isSafe(avail, maxm, allot, P)
    if is_safe:
        print("The system currently is on a safe state")
        print("Safe sequence:", safe_seq, '\n')
    else:
        print("The system is not in a safe state")
        exit(0)
            
    while True:
        pr = random.randint(0, P-1)
        request = np.array([random.randint(0, maxm[pr][i]) for i in range(R)])
        succesful_request = tryARequest(pr, request, avail, maxm, allot)
        is_safe, safe_seq = isSafe(avail, maxm, allot, P)
        if is_safe and succesful_request:
            print('The system with the satisfied request is on a safe state')
            print("Safe sequence:", safe_seq, '\n')
            exit(0)
        else:
            if not succesful_request:
                print('The system can not satisfy the request\n')
            else:
                print('The system is not on a safe state with the satisfied request\n')
                releaseResources(pr, request, avail, allot, calculateNeed(maxm, allot))
