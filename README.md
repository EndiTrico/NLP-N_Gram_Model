# NLP N-Gram Model

## Introduction
- In this assignment you will do some basic natural language processing (NLP). You will write a program which will construct a language model (for several different languages) and, when asked to classify a text, should select the language whose model is closer to the text in question. The language model (described below) will be constructed from text files.
- You will be required to achieve this task without using any loops (use streams and lambdas instead). After reading all your content into your representation structure, you also need to process said content via multithreading and update the model concurrently.

## Folder Structure
- Your program will read a local folder. This local folder will be given as a command line argument.
- The folder will have the following structure:</br>
• Local Folder</br>
  - mystery.txt</br>
  - lang-xx (e.g. al)</br>
    - file1-1.txt</br>
    - file1-2.txt</br>
    - ...</br>
  - lang-yy (e.g. en)</br>
    - file2-1.txt</br>
    - file2-2.txt</br>
    - ...</br>
  - lang-zz (e.g. fr)</br>
    - file3-1.txt</br>
    - file3-2.txt</br>
    - ...</br>
  - ...</br>
  - ...</br>
  - lang-kk (e.g. de)</br>
    - filek-1.txt</br>
    - filek-2.txt</br>
    - ...</br>
</br></br>
- The local folder will have one single text file mystery.txt and a number of subfolders (at least 2), one for each language.
- You can expect subfolders to be named with two latter code (al – Albanian, en – English, it – Italian, de – German etc.) From each language subfolder, you should only read files that end in .txt suffix and ignore any other files or subfolders.

## N-Gram Model
- When constructing the language model, you will use a model called n-gram. An n-gram in NLP is a sequence of n linguistic items. Such items can be words, sentences, or other components, but for this example you will use letters in a word.
- Usual values of n in n-gram models when applied to NLP are n = 1 (unigram), n = 2 (bigrams) or n = 3 (trigrams).
- Unigram model is the simplest, for every word w of length n, w = w<sub>0</sub>w<sub>1</sub>, ..., w<sub>n − 1</sub>, the unigram sequence is simply its individual letters w<sub>0</sub>, w<sub>1</sub>, ..., w<sub>n − 1</sub>.
  - As an example apple produces the unigram sequence a, p, p, l, e.
- Bigram model considers pairs of adjacent letters in a word, i.e. w<sub>0</sub>w<sub>1</sub>, w<sub>1</sub>w<sub>2</sub>, ..., w<sub>n − 2</sub>w<sub>n − 1</sub>.
  - The sample apple example produces the bigram sequence ap, pp, pl, le.
- Finally, the trigram model considers triples of adjacent letters in a word, i.e. w<sub>0</sub>w<sub>1</sub>w<sub>2</sub>, w<sub>1</sub>w<sub>2</sub>w<sub>3</sub>, ..., w<sub>n − 3</sub>w<sub>n − 2</sub>w<sub>n − 1</sub>.
  - Word apple then produces the trigram sequence app, ppl, ple.
- Your language model will then read value of n-gram. You should assume a default value n = 2 if it is not supplied.

## Language Model
- When building the n-gram based language model, you will read all the text files for that language, filter out punctuation, standardize into lower case, tokenize into individual words, process the items in their n-gram sequence and construct the histogram, or frequency distribution, of these items.
- E.g. in the unigram model, you will be counting the occurrences of each word in the words of the text whereas in the bigram model, you will be counting occurrences of pairs of letters.

## Document Distance
- Document distance (also known as cosine similarity) quantifies how similar two texts (more precisely their n-gram frequency distributions) are to one another.
- Without loss of generality we can treat the unigram frequency distribution as a vector A = [a<sub>1</sub>, a<sub>2</sub>, ..., a<sub>n</sub>], where each a<sub>i</sub> represents the frequency that the term i appears in the language model.
  - For example a<sub>th</sub>= 978 must be interpreted as the fact that the bigram th appears 978 times in the texts of the language.
- Then given two such vectors (i.e. frequency distributions) A and B, their similarity S is computed as:
```math
S=\frac{A \cdot B}{\|A\| \cdot\|B\|}=\frac{\sum_{i=1}^n a_i b_i}{\sqrt{\sum_{i=1}^n a_i^2} \cdot \sqrt{\sum_{i=1}^n b_i^2}}
```
</br>

- This measure falls in the interval [−1, 1], where value 1 indicates “exactly the same”, value -1 indicates “exactly the opposite”, and value 0 indicates orthogonality.
- We can visualize A and B as vectors in some hyperspace, then S is the cosine of the angle α between these two vectors.
<div align="center">
S = cos (α)</br>
α = cos⁻¹(S)
</div>
</br>

- When A and B represent the same document, they are equal to one another, which means their angle is 0 (remember cos(0) = 1). The further apart the vectors are from one another, the larger the angle, and its cosine value moves away from 1.

### Example

- Let us work these concepts through an example. Assume that we have constructed the following models
  - A = {a=2, b=3, d=1, e=4}
  - B = {a=1, c=3, d=2, e=2}
- First notice that both documents are of different size (A has a total of 10 items, B has only 8), and that some items but appear in A but not B and vice versa. Dot-product of A and B is then the multiplication of occurrences of each symbol in A and B respectively
<div align="center">
A ∙ B = 2 ∙ 1 + 1 ∙ 2 + 4 ∙ 2 = 12
</div>
</br>

- The norm of each vector is the square root of the sum of squares.</br>
```math
‖A‖ = \sqrt{2^2 + 3^2 + 1^2 + 4^2} = \sqrt{30} = 5.47
```
```math
‖B‖ = \sqrt{1^2 + 3^2 + 2^2 + 2^2} = \sqrt{18} = 4.24
```

- Then we compute similarity as S = $\( \frac{12}{\sqrt{30} \cdot \sqrt{18}} \)$ and distance angle α = 58.9°.

## Text Classification
- We can finally wrap the whole problem. Whenever presented with the necessary data (local folder, all the language folder, all the files in each folder, the text file to be classified and the n-gram dimension value), your program should construct a model for every language and the mystery text to be classified.
- Compute the similarity between the mystery text and each language and select the language with the highest similarity (equivalently smallest angle distance).

## Non-Functional Requirements
- Your program must ideally not contain any loops (for, while, do-while) at all. For iteration needs use streams. Also keep in mind that streams use lambdas and lambdas must be kept short and easy to read.
- All the language folders must be processed concurrently. All the text files in the folder must be processed concurrently. This means that multiple thread reading different documents of the same language, will try to access the language model data structures concurrently. You need to apply synchronization techniques to ensure thread-safety and ultimately correct behavior of the program.
