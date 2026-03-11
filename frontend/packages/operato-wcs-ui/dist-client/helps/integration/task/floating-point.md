# Floating Point

The IEEE Standard for Floating-Point Arithmetic (IEEE 754) is a technical standard for floating-point arithmetic established in 1985 by the Institute of Electrical and Electronics Engineers (IEEE). The standard addressed many problems found in the diverse floating-point implementations that made them difficult to use reliably and portably. Many hardware floating-point units use the IEEE 754 standard.

## parameters

- [accessor](../concept/data-accessor)
  - specify the value to be processed
- operation
  - read : Read IEEE754 floating point numbers from a Buffer
  - write : Write IEEE754 floating point numbers to a Buffer
- endian
  - little : little endian
  - big : big endian
- float type
  - float : 4byte single precision floating point
  - double : 8byte double precision floating point

## pseudo code

```
  const isLittleEndian = endian === 'little'
  const nBytes = floatType === 'float' ? 4 : 8
  const mantissa = floatType === 'float' ? 23 : 52

  var result

  if(operation == 'write') {
    result = Buffer.alloc(nBytes)
    ieee754.write(result, value, 0, isLittleEndian, mantissa, nBytes)
  } else {
    result = ieee754.read(value, 0, isLittleEndian, mantissa, nBytes)
  }
```
