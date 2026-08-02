with open('.github/workflows/pipeline.yml', 'r') as f:
    content = f.read()

target = '''    - name: Generate Temporary Keystore
      run: |
        keytool -genkey -v -keystore release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload -storepass temp1234 -keypass temp1234 -dname "CN=GitHub, OU=Actions, O=CI, L=Internet, ST=Global, C=US"
      
    - name: Build Release APK
      env:
        KEYSTORE_PATH: ${{ github.workspace }}/release.jks
        STORE_PASSWORD: temp1234
        KEY_PASSWORD: temp1234
      run: ./gradlew assembleRelease'''

replacement = '''    - name: Decode Keystore
      env:
        ENCODED_KEYSTORE: ${{ secrets.KEYSTORE_BASE64 }}
      run: |
        if [ -n "$ENCODED_KEYSTORE" ]; then
          echo "$ENCODED_KEYSTORE" | base64 -d > release.jks
        else
          echo "KEYSTORE_BASE64 secret is not set, falling back to temporary keystore"
          keytool -genkey -v -keystore release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload -storepass temp1234 -keypass temp1234 -dname "CN=GitHub, OU=Actions, O=CI, L=Internet, ST=Global, C=US"
        fi
      
    - name: Build Release APK
      env:
        KEYSTORE_PATH: ${{ github.workspace }}/release.jks
        STORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD || 'temp1234' }}
        KEY_PASSWORD: ${{ secrets.KEY_PASSWORD || 'temp1234' }}
        KEY_ALIAS: ${{ secrets.KEY_ALIAS || 'upload' }}
      run: ./gradlew assembleRelease'''

content = content.replace(target, replacement)

with open('.github/workflows/pipeline.yml', 'w') as f:
    f.write(content)
