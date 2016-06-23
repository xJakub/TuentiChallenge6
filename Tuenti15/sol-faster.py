import gmpy2 as gmpy2
from gmpy2 import mpz, xmpz, mpq, mpfr, mpc

#
#  Multiply two matrices
#  If a mask is given, ignore the positions set to False,
#  in order to speed up the processing.
#
def matrixMultiply(a, b, mask=None):
    resultRows = len(a)
    resultCols = len(b[0])
    aCols = len(a[0])
    
    result = [[mpz(0) for x in xrange(resultCols)] for x in xrange(resultRows)]
    for destRow in xrange(resultRows):
        if mask == None or mask[destRow]:
            for destCol in xrange(resultCols):
                if mask == None or mask[destCol]:
                    for aCol in xrange(aCols):
                        if mask == None or mask[aCol]:
                            result[destRow][destCol] += a[destRow][aCol] * b[aCol][destCol]
    return result


#
#  Simplify the entire matrix, given the common denominator
#
def matrixSimplify(mat, den):
    resultRows = len(mat)
    resultCols = len(mat[0])
    result = [[mpz(0) for x in xrange(resultCols)] for x in xrange(resultRows)]
    gcd = den
    
    for row in xrange(resultRows):
        for col in xrange(resultCols):
            gcd = gmpy2.gcd(gcd, mat[row][col])
            
    for row in xrange(len(mat)):
        for col in xrange(len(mat[0])):
            result[row][col] = mat[row][col] / gcd
            
    resultDen = den / gcd
    return [result, resultDen]

    
#
#  Given a starting chair, detect if we will repeat matrices
#  To accomplish this, we store the first <chairsCount>*2 steps,
#  checking if a step is identical to a previous one
#
#  This function also calculates the mask for matrixMultiply
#
def detectLoops(chair, possiblesMask):
    lastNumerators = [probabilities[chair]]
    lastDenominator = probabilitiesDenominator
    stepStates = [matrixSimplify(lastNumerators, lastDenominator)]
    [lastNumerator, lastDenominator] = stepStates[0]
    
    possiblesMask[chair] = True
    
    for step in xrange(1, chairsCount*2):
        newNumerators = matrixMultiply(lastNumerators, probabilities)
        newDenominator = lastDenominator * probabilitiesDenominator
        stepState = matrixSimplify(newNumerators, newDenominator)
        
        for x in xrange(0, chairsCount):
            if newNumerators[0][x] != 0:
                possiblesMask[x] = True
        
        if stepState in stepStates:
            oldStep = stepStates.index(stepState)
            return [oldStep, step-oldStep]
        
        stepStates.append(stepState)
        lastNumerators = newNumerators
        lastDenominator = newDenominator
    
    return [False, False]

    
fb = open('submitInput.sql', 'r')
lines = fb.readlines()

chairsCount = int(lines[0])
probabilitiesCount = int(lines[1])

# Read the probabilities
probabilities = [[mpz(0) for x in xrange(chairsCount)] for x in xrange(chairsCount)]
probabilities2 = [[0 for x in xrange(chairsCount)] for x in xrange(chairsCount)]

for probabilityIndex in xrange(probabilitiesCount):
    line = lines[probabilityIndex + 2]
    [chairFrom, chairTo, prob] = [int(x) for x in line.replace('/100', '').split()]
    probabilities[chairFrom][chairTo] = mpz(prob)
    probabilitiesDenominator = mpz(100)

cases = int(lines[2 + probabilitiesCount])

# Process every case
for caseNumber in xrange(1, cases+1):
    line = lines[caseNumber + probabilitiesCount + 2]
    [chair, steps] = [int(x) for x in line.split()]
    
    possiblesMask = [False for x in xrange(chairsCount)]
    [loopStart, loopPeriod] = detectLoops(chair, possiblesMask)
    
    if loopStart != False and steps > loopStart+loopPeriod:
        steps = loopStart + ((steps-loopStart) % loopPeriod)
    
    numerators = [[mpz(0) for x in xrange(chairsCount)]]
    numerators[0][chair] = mpz(1)
    denominator = mpz(1)
    
    powProbabilities = probabilities
    powDenominator = probabilitiesDenominator
    
    
	# numerators = v*A, n times
	# which is v*(A^n)
    # we use a fast pow method
    while steps > 0:
        [powProbabilities,powDenominator] = matrixSimplify(powProbabilities, powDenominator)
        
        if steps % 2 != 0:
            numerators = matrixMultiply(numerators, powProbabilities)
            denominator *= powDenominator
            steps -= 1
            
        steps /= 2
        if steps >= 1:
            powProbabilities = matrixMultiply(powProbabilities, powProbabilities, possiblesMask)
            powDenominator *= powDenominator
        
        
    # get the most probable chair
    bestChair = 0
    for i in xrange(1, chairsCount):
        if numerators[0][i] >= numerators[0][bestChair]:
            bestChair = i
        
    # and simplify the fraction
    numerator = numerators[0][bestChair]
    gcd = gmpy2.gcd(numerator, denominator)
    
    numerator /= gcd
    denominator /= gcd
    numDigit = str(numerator % 10)
    demDigit = str(denominator % 10)
    
    print "Case #%d: Chair: %d Last digits: %s/%s" % (caseNumber, bestChair, numDigit, demDigit)
    