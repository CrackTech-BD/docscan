#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint docscan.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'docscan'
  s.version          = '1.0.0'
  s.summary          = 'Plugin to detect edges of objects'
  s.description      = <<-DESC
Plugin to detect edges of objects
                       DESC
  s.homepage         = 'http://zuhabul.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Md Zuhabul Islam' => 'mail@zuhabul.com' }
  s.resources        = 'Assets/**/*'
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.dependency 'Flutter'
  s.dependency 'WeScan'
  s.platform = :ios, '10.0'

  # Flutter.framework does not contain a i386 slice.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386' }
  s.swift_version = '5.0'
end
